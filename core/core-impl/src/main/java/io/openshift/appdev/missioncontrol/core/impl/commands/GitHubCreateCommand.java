package io.openshift.appdev.missioncontrol.core.impl.commands;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.StatusMessage;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;
import io.openshift.appdev.missioncontrol.core.api.commands.AbstractCommand;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;

/**
 * Command that creates a github repo
 */
@ApplicationScoped
public class GitHubCreateCommand extends AbstractCommand {

    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    public GitHubCreateCommand(GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        super(statusEvent);
        this.gitHubServiceFactory = gitHubServiceFactory;
    }

    @Override
    public StatusMessage getStatusMessage() {
        return StatusMessage.GITHUB_CREATE;
    }

    @Override
    public void execute(CreateProjectile projectile) {
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
