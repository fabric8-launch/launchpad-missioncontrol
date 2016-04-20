package org.kontinuity.catapult.service.github.spi;

import java.io.IOException;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;

public interface GitHubServiceSpi extends GitHubService {

    /**
     * Creates a repository with the given information.
     *
     * @param repositoryName - the name of the repository
     * @param description - the repository description
     * @param homepage - the homepage url
     * @param has_issues - flag for whether issue tracking should be created
     * @param has_wiki - flag for whether a wiki should be created
     * @param has_downloads - flag for whether downloads should be created
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     */
    GitHubRepository create(String repositoryName, String description, String homepage,
        boolean has_issues, boolean has_wiki, boolean has_downloads) throws IOException, IllegalArgumentException;


    /**
     * Delete a repository specified by its value object representation.
     *
     * @param repository - the value objct the represents the Gitub repository
     * @throws IllegalArgumentException
     */
    void deleteRepository(final GitHubRepository repository) throws IllegalArgumentException;
    
}
