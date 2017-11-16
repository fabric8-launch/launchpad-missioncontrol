package io.openshift.appdev.missioncontrol.core.impl.observers;

import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.StatusEventType;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;
import io.openshift.appdev.missioncontrol.core.api.Step;
import io.openshift.appdev.missioncontrol.core.api.AbstractCommand;
import io.openshift.appdev.missioncontrol.core.impl.MissionControlImpl;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;

import static io.openshift.appdev.missioncontrol.core.api.StatusEventType.GITHUB_WEBHOOK;

/**
 * Creates a webhook on the github repo to fire a build / deploy when changes happen on the project.
 */
@ApplicationScoped
public class GitHubWebhookStepObserver extends AbstractCommand {

    private Logger log = Logger.getLogger(GitHubWebhookStepObserver.class.getName());

    private final OpenShiftServiceFactory openShiftServiceFactory;

    private final OpenShiftClusterRegistry openShiftClusterRegistry;

    private final GitHubServiceFactory gitHubServiceFactory;

    @Inject
    public GitHubWebhookStepObserver(OpenShiftServiceFactory openShiftServiceFactory,
                                     OpenShiftClusterRegistry openShiftClusterRegistry,
                                     GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        super(statusEvent);
        this.openShiftServiceFactory = openShiftServiceFactory;
        this.openShiftClusterRegistry = openShiftClusterRegistry;
        this.gitHubServiceFactory = gitHubServiceFactory;
    }

    @Override
    protected StatusEventType getStatusEventType() {
        return StatusEventType.GITHUB_WEBHOOK;
    }

    public void execute(@Observes @Step(GITHUB_WEBHOOK) CreateProjectile projectile) {
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());

        OpenShiftProject openShiftProject = openShiftService.findProject(projectile.getOpenShiftProjectName()).get();
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = gitHubService.getRepository(projectile.getGitHubRepositoryName());

        MissionControlImpl.getGitHubWebhooks(gitHubService, openShiftService, gitHubRepository, openShiftProject);
        fireEvent(projectile.getId());
    }

}
