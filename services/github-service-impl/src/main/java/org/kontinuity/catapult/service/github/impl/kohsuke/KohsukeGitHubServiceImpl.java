package org.kontinuity.catapult.service.github.impl.kohsuke;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;
import org.kontinuity.catapult.service.github.api.GitHubWebhookEvent;
import org.kontinuity.catapult.service.github.api.NoSuchRepositoryException;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;

/**
 * Implementation of {@link GitHubService} backed by the Kohsuke GitHub Java Client
 * http://github-api.kohsuke.org/
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
final class KohsukeGitHubServiceImpl implements GitHubService, GitHubServiceSpi {

    private static final String GITHUB_WEBHOOK_WEB = "web";

    private static final Logger log = Logger.getLogger(KohsukeGitHubServiceImpl.class.getName());

    private static final String MSG_NOT_FOUND = "Not Found";

    private final GitHub delegate;

    /**
     * Creates a new instance with the specified, required delegate
     *
     * @param delegate
     */
    KohsukeGitHubServiceImpl(final GitHub delegate) {
        assert delegate != null : "delegate must be specified";
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHubRepository fork(final String repositoryFullName) throws NoSuchRepositoryException,
            IllegalArgumentException {
        // Precondition checks
        if (repositoryFullName == null || repositoryFullName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }

        // First get the source repo
        final GHRepository source, target;
        try {
            source = delegate.getRepository(repositoryFullName);
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not fork specified repository "
                        + repositoryFullName + " because it could not be found.");
            }
            throw new RuntimeException("Could not fork " + repositoryFullName, ioe);
        }

        // Fork
        try {
            target = source.fork();
        } catch (final IOException ioe) {
            throw new RuntimeException("Could not fork requested repository " + repositoryFullName, ioe);
        }

        // Block on the full creation of the new fork, which is an async op
        GHRepository newlyCreatedRepo = null;
        final String targetRepoFullName = target.getFullName();
        for (int i = 0; i < 10; i++) {
            try {
                newlyCreatedRepo = delegate.getRepository(targetRepoFullName);
            } catch (final IOException ioe) {
                // Throw an exception if this error is anything other than not found repo
                if (!this.isRepoNotFound(ioe)) {
                    throw new RuntimeException("Error in not find newly-created repo " + targetRepoFullName, ioe);
                }
            }

            // Still no repo?  Sleep a bit and try again
            if (newlyCreatedRepo == null) {
                try {
                    Thread.sleep(3000);
                } catch (final InterruptedException ie) {
                    Thread.interrupted();
                    throw new RuntimeException(ie);
                }
            }
        }
        // Still can't find it after a few tries and waiting?  Fail.
        if (newlyCreatedRepo == null) {
            throw new RuntimeException(repositoryFullName + " was forked into " + targetRepoFullName +
                    " but can't find the new repository");
        }

        // Wrap in our API view and return
        final GitHubRepository wrapped = new KohsukeGitHubRepositoryImpl(newlyCreatedRepo);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Forked " + source.getFullName() + " as " + newlyCreatedRepo.getFullName() +
                    " available at " + newlyCreatedRepo.getGitTransportUrl());
        }
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitHubRepository createRepository(String repositoryName, String description, String homepage,
                                   boolean has_issues, boolean has_wiki, boolean has_downloads)
            throws IOException, IllegalArgumentException {
        // Precondition checks
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }

        GHRepository newlyCreatedRepo = delegate.createRepository(repositoryName)
                .description(description)
                .private_(false)
                .homepage(homepage)
                .issues(has_issues)
                .downloads(has_downloads)
                .wiki(has_wiki)
                .create();

        // Wrap in our API view and return
        final GitHubRepository wrapped = new KohsukeGitHubRepositoryImpl(newlyCreatedRepo);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Created " + newlyCreatedRepo.getFullName() + " available at "
                    + newlyCreatedRepo.getGitTransportUrl());
        }
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    public GitHubWebhook createWebhook(final GitHubRepository repository,
                                       final URL webhookUrl,
                                       final GitHubWebhookEvent... events)
            throws IllegalArgumentException {
        // Precondition checks
        if (repository == null) {
            throw new IllegalArgumentException("repository must be specified");
        }
        if (webhookUrl == null) {
            throw new IllegalArgumentException("webhook URL must be specified");
        }
        if (events == null || events.length == 0) {
            throw new IllegalArgumentException("at least one event must be specified");
        }

    	final GHRepository repo;
        try {
            repo = delegate.getRepository(repository.getFullName());
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
        Map<String, String> configuration = new HashMap<>();
    	configuration.put("url", webhookUrl.toString());
    	configuration.put("content_type", "json");

        List<GHEvent> githubEvents = Stream.of(events).map(event -> GHEvent.valueOf(event.name())).collect(Collectors.toList());

        final GHHook webhook;
        try {
            webhook = repo.createHook(
                    GITHUB_WEBHOOK_WEB,
                    configuration,
                    githubEvents,
                    true);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }

        final GitHubWebhook githubWebhook = new KohsukeGitHubWebhook(webhook);
    	return githubWebhook;
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteWebhooks(final GitHubRepository repository) throws IllegalArgumentException {
    	if(repository == null) {
    		throw new IllegalArgumentException("repository must be specified");
    	}
    	final GHRepository repo;
    	try {
    		repo = delegate.getRepository(repository.getFullName());
    		
    		for (GHHook hook: repo.getHooks()) {
        		hook.delete();
        	}
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not remove webhooks from specified repository "
                        + repository.getFullName() + " because it could not be found or there is no webhooks for that repository.");
            }
            throw new RuntimeException("Could not remove webhooks from " + repository.getFullName(), ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteRepository(final GitHubRepository repository) throws IllegalArgumentException {
    	if(repository == null) {
    		throw new IllegalArgumentException("repository must be specified");
    	}
    	final GHRepository repo;
    	try {
    		repo = delegate.getRepository(repository.getFullName());
    		repo.delete();
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not remove repository "
                        + repository.getFullName() + " because it could not be found.");
            }
            throw new RuntimeException("Could not remove " + repository.getFullName(), ioe);
        }
    }

    /**
     * Determines if the required {@link IOException} in question represents a repo
     * that can't be found
     *
     * @param ioe
     * @return
     */
    private boolean isRepoNotFound(final IOException ioe) {
        assert ioe != null : "ioe is required";
        return (ioe.getClass() == FileNotFoundException.class &&
                ioe.getMessage().contains(MSG_NOT_FOUND));
    }
}
