package io.openshift.appdev.missioncontrol.core.impl.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.StatusEventType;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;
import io.openshift.appdev.missioncontrol.core.api.Step;
import io.openshift.appdev.missioncontrol.core.api.AbstractCommand;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;

import static io.openshift.appdev.missioncontrol.core.api.StatusEventType.GITHUB_CREATE;

/**
 * Command that creates a github repo
 */
@ApplicationScoped
public class GitHubCreateStepObserver extends AbstractCommand {

    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    public GitHubCreateStepObserver(GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        super(statusEvent);
        this.gitHubServiceFactory = gitHubServiceFactory;
    }

    @Override
    protected StatusEventType getStatusEventType() {
        return StatusEventType.GITHUB_CREATE;
    }

    public void execute(@Observes @Step(GITHUB_CREATE) CreateProjectile projectile) {
        String repositoryDescription = projectile.getGitHubRepositoryDescription();
        String repositoryName = projectile.getGitHubRepositoryName();
        if (repositoryName == null) {
            repositoryName = projectile.getOpenShiftProjectName();
        }

        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = gitHubService.createRepository(repositoryName, repositoryDescription);
        fireEvent(projectile.getId(), gitHubRepository.getHomepage());
    }
}
