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
public final class KohsukeGitHubServiceImpl implements GitHubService, GitHubServiceSpi {

    private static final String WEBHOOK_URL = "url";

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
     * @throws IOException 
     */
    @Override
    public boolean repositoryExists(String repositoryName) {
    	try {
    		return this.delegate.getRepository(repositoryName) != null;
    	} catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (KohsukeGitHubServiceImpl.isRepoNotFound(ioe)) {
                return false;
            }
            throw new RuntimeException("Could not fork " + repositoryName, ioe);
        }
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
        final GHRepository source;
        try {
            source = delegate.getRepository(repositoryFullName);
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (KohsukeGitHubServiceImpl.isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not fork specified repository "
                        + repositoryFullName + " because it could not be found.");
            }
            throw new RuntimeException("Could not fork " + repositoryFullName, ioe);
        }

        // Fork (with retries as something is wonky here)
       GHRepository newlyCreatedRepo = null;
       final int maxRetries = 10;
       for (int i = 0; i < maxRetries; i++) {
          try {
             newlyCreatedRepo = source.fork();
             break;
          } catch (final IOException ioe) {
             log.info("Trying fork operation again: " + i + " due to: " + ioe.getMessage());
             try {
                Thread.sleep(3000);
             } catch (final InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException("Interrupted while waiting for fork retry", e);
             }
          }
       }
       if (newlyCreatedRepo == null) {
          throw new IllegalStateException("Newly created repo must be assigned; programming error");
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
    public GitHubRepository createRepository(String repositoryName,
                                   String description) throws IllegalArgumentException {
        // Precondition checks
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("repository description must be specified");
        }

        GHRepository newlyCreatedRepo = null;
		try {
			newlyCreatedRepo = delegate.createRepository(repositoryName)
			        .description(description)
			        .private_(false)
			        .homepage("")
			        .issues(false)
			        .downloads(false)
			        .wiki(false)
			        .create();
		} catch (IOException e) {
			throw new RuntimeException("Could not create GitHub repository named '" + repositoryName + "'", e);
		}

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
    @Override
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
        log.info("Adding webhook at '" + webhookUrl.toExternalForm()+ "' on repository '" + repository.getFullName() + "'");
		
    	final GHRepository repo;
        try {
            repo = delegate.getRepository(repository.getFullName());
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
        Map<String, String> configuration = new HashMap<>();
    	configuration.put(WEBHOOK_URL, webhookUrl.toString());
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
    @Override
    public void deleteWebhook(final GitHubRepository repository, GitHubWebhook webhook) throws IllegalArgumentException {
    	if(repository == null) {
    		throw new IllegalArgumentException("repository must be specified");
    	}
        if(webhook == null) {
            throw new IllegalArgumentException("webhook must be specified");
        }
    	final GHRepository repo;
    	try {
    		repo = delegate.getRepository(repository.getFullName());
    		
    		for (GHHook hook: repo.getHooks()) {
			if(hook.getConfig().get(WEBHOOK_URL).equals(webhook.getUrl())) {
				hook.delete();
				break;
			}
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
    @Override
    public void deleteRepository(final GitHubRepository repository) throws IllegalArgumentException {
    	deleteRepository(repository.getFullName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRepository(final String repositoryName) throws IllegalArgumentException {
    	if(repositoryName == null) {
    		throw new IllegalArgumentException("repositoryName must be specified");
    	}
    	try {
			final GHRepository repo = delegate.getRepository(repositoryName);
			log.warning("Deleting repo at " + repo.gitHttpTransportUrl());
			repo.delete();
        } catch (final IOException ioe) {
            // Check for repo not found (this is how Kohsuke Java Client reports the error)
            if (isRepoNotFound(ioe)) {
                throw new NoSuchRepositoryException("Could not remove repository "
                        + repositoryName + " because it could not be found.");
            }
            throw new RuntimeException("Could not remove " + repositoryName, ioe);
        }
    }
    /**
     * Determines if the required {@link IOException} in question represents a repo
     * that can't be found
     *
     * @param ioe
     * @return
     */
    private static boolean isRepoNotFound(final IOException ioe) {
        assert ioe != null : "ioe is required";
        return ioe.getClass() == FileNotFoundException.class &&
                ioe.getMessage().contains(MSG_NOT_FOUND);
    }
    
}
