package io.openshift.appdev.missioncontrol.core.impl.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.apache.commons.lang.text.StrSubstitutor;

/**
 * Command that creates a github repo
 */
@ApplicationScoped
public class GitHubPushCommand extends AbstractCommand {

    private static final Logger log = Logger.getLogger(GitHubPushCommand.class.getName());
    private final GitHubServiceFactory gitHubServiceFactory;

    @Inject
    GitHubPushCommand(GitHubServiceFactory gitHubServiceFactory, Event<StatusMessageEvent> statusEvent) {
        super(statusEvent);
        this.gitHubServiceFactory = gitHubServiceFactory;
    }

    @Override
    public StatusMessage getStatusMessage() {
        return StatusMessage.GITHUB_PUSHED;
    }

    @Override
    public void execute(CreateProjectile projectile) {
        GitHubService gitHubService = gitHubServiceFactory.create(projectile.getGitHubIdentity());
        GitHubRepository gitHubRepository = gitHubService.getRepository(projectile.getGitHubRepositoryName());
        File path = projectile.getProjectLocation().toFile();

        // Add logged user in README.adoc
        File readmeAdoc = new File(path, "README.adoc");
        if (readmeAdoc.exists()) {
            try {
                String content = new String(Files.readAllBytes(readmeAdoc.toPath()));
                Map<String, String> values = new HashMap<>();
                values.put("loggedUser", gitHubService.getLoggedUser().getLogin());
                String newContent = new StrSubstitutor(values).replace(content);
                Files.write(readmeAdoc.toPath(), newContent.getBytes());
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error while replacing README.adoc variables", e);
            }
        }

        gitHubService.push(gitHubRepository, path);
        fireEvent(projectile.getId());
    }
}
