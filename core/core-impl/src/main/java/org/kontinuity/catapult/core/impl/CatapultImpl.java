package org.kontinuity.catapult.core.impl;

import java.io.BufferedReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.CreateProjectile;
import org.kontinuity.catapult.core.api.ForkProjectile;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.service.github.api.DuplicateWebhookException;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubServiceFactory;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;
import org.kontinuity.catapult.service.github.api.GitHubWebhookEvent;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.api.OpenShiftServiceFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Implementation of the {@link Catapult} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class CatapultImpl implements Catapult {

    private static final Logger log = Logger.getLogger(CatapultImpl.class.getName());

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Boom fling(final ForkProjectile projectile) throws IllegalArgumentException {

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
          TODO Figure how to best handle posilbe DuplicateProjectException, but has to be handled to the user at some intelligent level
         */
        OpenShiftService openShiftService = openShiftServiceFactory.create(projectile.getOpenShiftIdentity());
        final OpenShiftProject createdProject = openShiftService.createProject(projectName);

        ForkProjectile forkProjectile = projectile;
        /*
         * Construct the full URI for the pipeline template file,
         * relative to the repository root
         */
        final URI pipelineTemplateUri = UriBuilder.fromUri("https://raw.githubusercontent.com/")
                .path(forkProjectile.getSourceGitHubRepo())
                .path(forkProjectile.getGitRef())
                .path(forkProjectile.getPipelineTemplatePath()).build();

        // Configure the OpenShift project
        openShiftService.configureProject(createdProject,
                                          gitHubRepository.getGitCloneUri(),
                                          forkProjectile.getGitRef(),
                                          pipelineTemplateUri);

        GitHubWebhook webhook = getGitHubWebhook(gitHubService, openShiftService, gitHubRepository, createdProject);

        // Return information needed to continue flow to the user
        return new BoomImpl(gitHubRepository, createdProject, webhook);
    }

    @Override
    public Boom fling(CreateProjectile projectile) throws IllegalArgumentException {
        final GitHubService gitHubService = getGitHubService(projectile);
        String projectName = projectile.getOpenShiftProjectName();
        Path projectLocationPath = projectile.getProjectLocation();
        String repositoryDescription = guessRepositoryDescription(projectLocationPath);
        GitHubRepository gitHubRepository = gitHubService.createRepository(projectName, repositoryDescription);
        gitHubService.push(gitHubRepository, projectLocationPath.toFile());

        OpenShiftService openShiftService = openShiftServiceFactory.create(projectile.getOpenShiftIdentity());
        OpenShiftProject createdProject = openShiftService.createProject(projectName);
        openShiftService.configureProject(createdProject, gitHubRepository.getGitCloneUri());

        GitHubWebhook webhook = getGitHubWebhook(gitHubService, openShiftService, gitHubRepository, createdProject);
        return new BoomImpl(gitHubRepository, createdProject, webhook);
    }


    /**
     * Guess the Repository Description from the .obsidian/obsidian.yaml file.
     * Returns " " if not found
     *
     * @param path
     * @return
     */
    private String guessRepositoryDescription(Path projectLocation) {
        String path = " ";
        Path obsidianDescriptor = projectLocation.resolve(".obsidian/obsidian.yaml");
        if (Files.exists(obsidianDescriptor)) {
            Yaml yaml = new Yaml();
            try (BufferedReader reader = Files.newBufferedReader(obsidianDescriptor)) {
                Map<String, String> data = yaml.loadAs(reader, Map.class);
                path = data.getOrDefault("description", " ");
            } catch (Exception e) {
                // Ignore
                log.log(Level.FINEST, "Error while reading obsidian descriptor", e);
            }
        }
        return path;
    }


    private GitHubWebhook getGitHubWebhook(GitHubService gitHubService, OpenShiftService openShiftService,
                                           GitHubRepository gitHubRepository, OpenShiftProject createdProject) {
        GitHubWebhook webhook;
        final URL webhookUrl = openShiftService.getWebhookUrl(createdProject);
        if (webhookUrl != null) {
            try {
                webhook = gitHubService.createWebhook(gitHubRepository, webhookUrl, GitHubWebhookEvent.PUSH);
            } catch (final DuplicateWebhookException dpe) {
                // Swallow, it's OK, we've already forked this repo
                log.log(Level.INFO, dpe.getMessage());
                webhook = ((GitHubServiceSpi) gitHubService)
                        .getWebhook(gitHubRepository, webhookUrl);
            }
        } else {
            webhook = null;
        }
        return webhook;
    }

    private GitHubService getGitHubService(Projectile projectile) {
        if (projectile == null) {
            throw new IllegalArgumentException("projectile must be specified");
        }
        return gitHubServiceFactory.create(projectile.getGitHubIdentity());
    }
}
