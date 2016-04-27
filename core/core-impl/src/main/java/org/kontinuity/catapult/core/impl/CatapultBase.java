package org.kontinuity.catapult.core.impl;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.impl.kohsuke.GitHubServiceProducer;
import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.api.OpenShiftSettings;
import org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client.OpenShiftServiceProducer;

/**
 * {@inheritDoc}
 * <p>
 * May be extended to create managed components like EJBs or CDI beans
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public abstract class CatapultBase implements Catapult {

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
        final GitHubService gitHubService = new GitHubServiceProducer().create(gitHubAccessToken, null);
        final GitHubRepository forkedRepo = gitHubService.fork(sourceRepoName);

        // TODO: should we use the @Inject ? In which case, how do we pass the access token ?
        final String openShiftApiUrl = OpenShiftSettings.getOpenShiftUrl();
        final OpenShiftService openShiftService = new OpenShiftServiceProducer().create(openShiftApiUrl);
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
