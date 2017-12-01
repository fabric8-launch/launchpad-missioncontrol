package io.openshift.appdev.missioncontrol.core.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.TokenIdentity;
import io.openshift.appdev.missioncontrol.core.api.Boom;
import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.ForkProjectile;
import io.openshift.appdev.missioncontrol.core.api.LaunchEvent;
import io.openshift.appdev.missioncontrol.core.api.MissionControl;
import io.openshift.appdev.missioncontrol.core.api.Projectile;
import io.openshift.appdev.missioncontrol.core.api.StatusEventType;
import io.openshift.appdev.missioncontrol.core.api.Step;
import io.openshift.appdev.missioncontrol.service.github.api.DuplicateWebhookException;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhook;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhookEvent;
import io.openshift.appdev.missioncontrol.service.github.spi.GitHubServiceSpi;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;

/**
 * Implementation of the {@link MissionControl} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class MissionControlImpl implements MissionControl {

    private static final Logger log = Logger.getLogger(MissionControlImpl.class.getName());

    private static final String LOCAL_USER_ID_PREFIX = "LOCAL_USER_";

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private OpenShiftClusterRegistry openShiftClusterRegistry;

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private Event<Projectile> projectileEvent;

    @Inject
    private Event<LaunchEvent> launchEvent;

    /**
     * {@inheritDoc}
     */
    @Override
    public Boom launch(final ForkProjectile projectile) throws IllegalArgumentException {

        final GitHubService gitHubService = getGitHubService(projectile);
        GitHubRepository gitHubRepository;
        // Get properties
        final String sourceRepoName = projectile.getSourceGitHubRepo();
        gitHubRepository = gitHubService.fork(sourceRepoName);

        //TODO
        // https://github.com/redhat-kontinuity/catapult/issues/18
        // Create a new OpenShift project for the user
        final String projectName = projectile.getOpenShiftProjectName();

        /*
          TODO Figure how to best handle possible DuplicateProjectException, but has to be handled to the user at some intelligent level
         */
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        assert cluster.isPresent() : "OpenShift Cluster not found: " + projectile.getOpenShiftClusterName();
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());
        final OpenShiftProject createdProject = openShiftService.createProject(projectName);

        /*
         * Construct the full URI for the pipeline template file,
         * relative to the repository root
         */
        final URI pipelineTemplateUri = UriBuilder.fromUri("https://raw.githubusercontent.com/")
                .path(projectile.getSourceGitHubRepo())
                .path(projectile.getGitRef())
                .path(projectile.getPipelineTemplatePath()).build();

        // Configure the OpenShift project
        openShiftService.configureProject(createdProject,
                                          gitHubRepository.getGitCloneUri(),
                                          projectile.getGitRef(),
                                          pipelineTemplateUri);

        List<GitHubWebhook> webhooks = getGitHubWebhooks(gitHubService, openShiftService, gitHubRepository, createdProject);

        // Return information needed to continue flow to the user
        return new BoomImpl(gitHubRepository, createdProject, webhooks);
    }

    @Override
    public Boom launch(CreateProjectile projectile) throws IllegalArgumentException {
        int startIndex = projectile.getStartOfStep();

        StatusEventType[] statusEventTypes = StatusEventType.values();

        for (int i = startIndex; i < statusEventTypes.length; i++) {
            this.projectileEvent.select(new Step.Literal(statusEventTypes[i])).fire(projectile);
        }
        launchEvent.fire(new LaunchEvent(getUserId(projectile), projectile.getId(), projectile.getGitHubRepositoryName(),
                                         projectile.getOpenShiftProjectName(), projectile.getMission(), projectile.getRuntime()));
        return null;
    }

    private String getUserId(Projectile projectile) {
        final Identity identity = projectile.getOpenShiftIdentity();
        String userId;
        // User ID will be the token
        if (identity instanceof TokenIdentity) {
            userId = ((TokenIdentity) identity).getToken();
        } else {
            // For users authenticating with user/password (ie. local/Minishift/CDK)
            // let's identify them by their MAC address (which in a VM is the MAC address
            // of the VM, or a fake one, but all we can really rely on to uniquely identify
            // an installation
            final StringBuilder sb = new StringBuilder();
            try {
                byte[] macAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
                sb.append(LOCAL_USER_ID_PREFIX);
                for (int i = 0; i < macAddress.length; i++) {
                    sb.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
                }
                userId = sb.toString();
            } catch (Exception e) {
                userId = LOCAL_USER_ID_PREFIX + "UNKNOWN";
            }
        }
        return userId;
    }

    public static List<GitHubWebhook> getGitHubWebhooks(GitHubService gitHubService, OpenShiftService openShiftService,
                                                  GitHubRepository gitHubRepository, OpenShiftProject createdProject) {
        List<GitHubWebhook> webhooks = openShiftService.getWebhookUrls(createdProject).stream()
                .map(webhookUrl -> {
                    try {
                        return gitHubService.createWebhook(gitHubRepository, webhookUrl, GitHubWebhookEvent.PUSH);
                    } catch (final DuplicateWebhookException dpe) {
                        // Swallow, it's OK, we've already forked this repo
                        log.log(Level.INFO, dpe.getMessage());
                        return ((GitHubServiceSpi) gitHubService).getWebhook(gitHubRepository, webhookUrl);
                    }
                })
                .collect(Collectors.toList());
        return webhooks;
    }

    private GitHubService getGitHubService(Projectile projectile) {
        if (projectile == null) {
            throw new IllegalArgumentException("projectile must be specified");
        }
        return gitHubServiceFactory.create(projectile.getGitHubIdentity());
    }
}
