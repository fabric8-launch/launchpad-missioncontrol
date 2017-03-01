package org.kontinuity.catapult.core.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.ForkProjectile;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.service.github.api.*;
import org.kontinuity.catapult.service.github.impl.kohsuke.KohsukeGitHubServiceImpl;
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
    public Boom fling(final Projectile projectile1) throws IllegalArgumentException {

        // Preconditions
        if (projectile1 == null) {
            throw new IllegalArgumentException("projectile must be specified");
        }

        final ForkProjectile projectile = (ForkProjectile)projectile1;
        // Get properties
        final String sourceRepoName = projectile.getSourceGitHubRepo();

        // Fork the repository for the user
        final String gitHubAccessToken = projectile.getGitHubAccessToken();
        final GitHubService gitHubService = gitHubServiceFactory.create(gitHubAccessToken);
        final GitHubRepository forkedRepo = gitHubService.fork(sourceRepoName);

        //TODO
        // https://github.com/redhat-kontinuity/catapult/issues/18
        // Create a new OpenShift project for the user
        final String projectName = projectile.getOpenShiftProjectName();
        final OpenShiftProject createdProject;
        GitHubWebhook webhook;
        try {
            createdProject = openShiftService.createProject(projectName);

            /*
             * Construct the full URI for the pipeline template file,
             * relative to the repository root
             */
            final StringBuilder sb = new StringBuilder();
            sb.append("https://raw.githubusercontent.com/");
            sb.append(projectile.getSourceGitHubRepo());
            sb.append('/');
            sb.append(projectile.getGitRef());
            sb.append('/');
            sb.append(projectile.getPipelineTemplatePath());
            final URI pipelineTemplateUri;
            try {
                pipelineTemplateUri = new URI(sb.toString());
            } catch (final URISyntaxException urise) {
                throw new RuntimeException("Could not create URI for pipeline template path", urise);
            }
            // Configure the OpenShift project
            openShiftService.configureProject(createdProject,
                    forkedRepo.getGitCloneUri(),
                    projectile.getGitRef(),
                    pipelineTemplateUri);
            final URL webhookUrl = openShiftService.getWebhookUrl(createdProject);
            if (webhookUrl != null) {
                try {
                    webhook = gitHubService.createWebhook(forkedRepo, webhookUrl, GitHubWebhookEvent.PUSH);
                } catch (final DuplicateWebhookException dpe) {
                    // Swallow, it's OK, we've already forked this repo
                    log.log(Level.INFO, dpe.getMessage());
                    webhook = ((GitHubServiceSpi) gitHubService)
                            .getWebhook(forkedRepo, webhookUrl);
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
        final Boom boom = new BoomImpl(forkedRepo, createdProject, webhook);
        return boom;
    }
}
