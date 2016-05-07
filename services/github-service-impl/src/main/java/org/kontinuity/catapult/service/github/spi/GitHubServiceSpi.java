package org.kontinuity.catapult.service.github.spi;

import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;

/**
 * SPI on top of GitHubService to provide with operations that are not exposed
 * in the base API (e.g., for testing purpose).
 */
public interface GitHubServiceSpi extends GitHubService {

    /**
     * Creates a repository with the given information (name and description). The repository will be
     * created by default with no homepage, issues, wiki downloads and will be public.
     *
     * @param repositoryName - the name of the repository
     * @param description - the repository description
     * @return the created {@link GitHubRepository}
     * @throws IllegalArgumentException
     */
    GitHubRepository createRepository(String repositoryName, String description) throws IllegalArgumentException;


    /**
     * Delete a repository specified by its value object representation.
     *
     * @param repository - the value objct the represents the Gitub repository
     * @throws IllegalArgumentException
     */
    void deleteRepository(final GitHubRepository repository) throws IllegalArgumentException;
    
    /**
     * Deletes a webhook in a specific GitHub repository
     * 
     * @param repository - the value object that represents the GitHub repository
     * @param webhook - the value object that represents the GitHub webhook
     * @throws IllegalArgumentException If either parameter is unspecified
     */
    void deleteWebhook(final GitHubRepository repository, GitHubWebhook webhook) throws IllegalArgumentException;
    
}
