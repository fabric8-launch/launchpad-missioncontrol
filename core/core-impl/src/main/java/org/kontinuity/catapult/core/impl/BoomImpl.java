package org.kontinuity.catapult.core.impl;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;

/**
 * {@inheritDoc}
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class BoomImpl implements Boom{

    private final GitHubRepository gitHubRepository;
    private final OpenShiftProject openShiftProject;

    /**
     * Creates a new instance with the specified, required {@link GitHubRepository}
     * and {@link OpenShiftProject}
     * @param gitHubRepository
     * @param openShiftProject
     */
    BoomImpl(final GitHubRepository gitHubRepository, final OpenShiftProject openShiftProject){
        assert gitHubRepository!=null:"gitHubRepository must be specified";
        assert openShiftProject!=null:"openShiftProject must be specified";
        this.gitHubRepository = gitHubRepository;
        this.openShiftProject = openShiftProject;
    }


    @Override
    public GitHubRepository getCreatedRepository() {
        return gitHubRepository;
    }

    @Override
    public OpenShiftProject getCreatedProject() {
        return openShiftProject;
    }
}
