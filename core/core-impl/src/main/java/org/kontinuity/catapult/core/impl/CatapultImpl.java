package org.kontinuity.catapult.core.impl;

import java.net.URI;

import javax.inject.Inject;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubServiceFactory;
import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;

/**
 * Implementation of the {@link Catapult} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class CatapultImpl implements Catapult {

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

        // Get properties
        final String sourceRepoName = projectile.getSourceGitHubRepo();

        // Fork the repository for the user
        final String gitHubAccessToken = projectile.getGitHubAccessToken();
        final GitHubService gitHubService = gitHubServiceFactory.create(gitHubAccessToken);
        final GitHubRepository forkedRepo = gitHubService.fork(sourceRepoName);

        //TODO
        // https://github.com/redhat-kontinuity/catapult/issues/18
        // Create a new OpenShift project for the user
        final String forkedRepoName = forkedRepo.getFullName();
        final String projectName = forkedRepoName.substring(forkedRepoName.lastIndexOf('/') + 1);
        final OpenShiftProject createdProject;
        try {
            createdProject = openShiftService.createProject(projectName);
            final URI projectTemplateDownloadUri = forkedRepo.getDownloadUri(projectile.getOpenShiftProjectTemplateFileName());
            if(projectTemplateDownloadUri != null) { 
            	openShiftService.configureProject(createdProject, forkedRepo.getGitCloneUri(), projectTemplateDownloadUri);
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

        //TODO
        /* Register a GitHub webhook in the user's repo to kick the OpenShift project
            https://github.com/redhat-kontinuity/catapult/issues/40
        */

        // Return information needed to continue flow to the user
        final Boom boom = new BoomImpl(forkedRepo, createdProject);
        return boom;
    }
}
