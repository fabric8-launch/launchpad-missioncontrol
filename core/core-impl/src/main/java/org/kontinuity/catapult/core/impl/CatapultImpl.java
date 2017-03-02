package org.kontinuity.catapult.core.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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
import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;

/**
 * Implementation of the {@link Catapult} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class CatapultImpl implements Catapult {

    private static final Logger log = Logger.getLogger(CatapultImpl.class.getName());

	@Inject
	private OpenShiftService openShiftService;

	@Inject
	private GitHubServiceFactory gitHubServiceFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Boom fling(final Projectile projectile) throws IllegalArgumentException {

        // Preconditions
        if (projectile == null) {
            throw new IllegalArgumentException("projectile must be specified");
        }

        // Fork the repository for the user
        final String gitHubAccessToken = projectile.getGitHubAccessToken();
        final GitHubService gitHubService = gitHubServiceFactory.create(gitHubAccessToken);
        final GitHubRepository gitHubRepository;
        if (projectile instanceof ForkProjectile) {
            // Get properties
            final String sourceRepoName = ((ForkProjectile) projectile).getSourceGitHubRepo();
            gitHubRepository = gitHubService.fork(sourceRepoName);
        } else {
            gitHubRepository = gitHubService.createRepository(projectile.getOpenShiftProjectName(), "quickstart");
            String projectLocation = ((CreateProjectile) projectile).getProjectLocation();
            try {
                Git repo = Git.init().setDirectory(new File(projectLocation)).call();
                repo.add().addFilepattern(".").call();
                repo.commit().setMessage("initial version").call();
                RemoteAddCommand add = repo.remoteAdd();
                add.setName("origin");
                add.setUri(new URIish(gitHubRepository.getGitCloneUri().toURL()));
                add.call();
                repo.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitHubAccessToken, "")).call();
            } catch (GitAPIException | MalformedURLException e) {
                throw new RuntimeException("An error occurred while creating the git repo", e);
            }
        }

        //TODO
        // https://github.com/redhat-kontinuity/catapult/issues/18
        // Create a new OpenShift project for the user
        final String projectName = projectile.getOpenShiftProjectName();
        final OpenShiftProject createdProject;
        GitHubWebhook webhook;
        try {
            createdProject = openShiftService.createProject(projectName);

            if (projectile instanceof ForkProjectile) {
                ForkProjectile forkProjectile = (ForkProjectile)projectile;
                /*
                 * Construct the full URI for the pipeline template file,
                 * relative to the repository root
                 */
                final StringBuilder sb = new StringBuilder();
                sb.append("https://raw.githubusercontent.com/");
                sb.append(forkProjectile.getSourceGitHubRepo());
                sb.append('/');
                sb.append(forkProjectile.getGitRef());
                sb.append('/');
                sb.append(forkProjectile.getPipelineTemplatePath());
                final URI pipelineTemplateUri;
                try {
                    pipelineTemplateUri = new URI(sb.toString());
                } catch (final URISyntaxException urise) {
                    throw new RuntimeException("Could not create URI for pipeline template path", urise);
                }
                // Configure the OpenShift project
                openShiftService.configureProject(createdProject,
                      gitHubRepository.getGitCloneUri(),
                      forkProjectile.getGitRef(),
                      pipelineTemplateUri);
            } else {
                openShiftService.configureProject(createdProject, gitHubRepository.getGitCloneUri());
            }

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

        } catch (final DuplicateProjectException dpe) {
            //TODO
            /*
              Figure how to best handle this, which may in fact
               be letting the dpe throw up, but has to be handled to the user
               at some intelligent level
             */
            throw dpe;
        }

        // Return information needed to continue flow to the user
        return new BoomImpl(gitHubRepository, createdProject, webhook);
    }
}
