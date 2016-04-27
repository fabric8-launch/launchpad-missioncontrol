package org.kontinuity.catapult.core.impl;

import javax.inject.Inject;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.CatapultService;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.impl.kohsuke.GitHubServiceProducer;
import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;

/**
 * Implementation of the {@link CatapultService} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class CatapultServiceImpl implements CatapultService {

	@Inject
	private OpenShiftService openShiftService; 
	
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
        // TODO: should we use the @Inject ? In which case, how do we pass the access token ?
        // TODO: in other words, if the GitHubService cannot be injected, should we keep all the CDI enablement (beans.xml, producer, integration test with Arquillian, etc.) ?
        // TODO: or maybe the gitHubAccessToken
        final GitHubService gitHubService = new GitHubServiceProducer().create(gitHubAccessToken, null);
        final GitHubRepository forkedRepo = gitHubService.fork(sourceRepoName);

        //TODO
        // https://github.com/redhat-kontinuity/catapult/issues/18
        // Create a new OpenShift project for the user
        final String forkedRepoName = forkedRepo.getFullName();
        final String projectName = forkedRepoName.substring(forkedRepoName.lastIndexOf('/') + 1);
        final OpenShiftProject createdProject;
        try {
            createdProject = openShiftService.createProject(projectName);
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
