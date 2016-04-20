package org.kontinuity.catapult.service.github.api;

import java.io.IOException;
import java.net.URL;

/**
 * Defines the operations we support with the GitHub backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface GitHubService {

    /**
     * Forks the specified repository (in full name format "owner/repo") into the current user's namespace
     * and returns a reference to the
     * If the user already has a fork of the specified repository, a reference to it will be returned.
     *
     * @param repositoryFullName
     * @return The target repo forked from the source, or the user's existing fork of the source repo
     * @throws NoSuchRepositoryException If the specified repository does not exist
     * @throws IllegalArgumentException  If the repository name is not specified
     */
    GitHubRepository fork(String repositoryFullName) throws NoSuchRepositoryException,
            IllegalArgumentException;


    /**
     * Creates a webhook in the GitHub repository.
     *
     * @param repository - the value object that represents the GitHub repository
     * @param webhookUrl - the URL of the webhook
     * @param events - the events that trigger the webhook; at least one is required
     * @throws IllegalArgumentException If any of the parameters are unspecified
     * @return
     */
    GitHubWebhook createWebhook(GitHubRepository repository,
                                URL webhookUrl,
                                GitHubWebhookEvent... events)
            throws IllegalArgumentException;
    
}