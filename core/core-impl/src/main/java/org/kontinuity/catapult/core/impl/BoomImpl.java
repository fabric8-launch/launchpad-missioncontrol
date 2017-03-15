package org.kontinuity.catapult.core.impl;

import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class BoomImpl implements Boom {

    /**
     * Creates a new instance with the specified, required {@link GitHubRepository}
     * and {@link OpenShiftProject}
     *
     * @param gitHubRepository the forked repository on GitHub. Required
     * @param openShiftProject the project created on OpenShift. Required
     * @param webhook          the webhook created on the forked repo on GitHub to trigger builds on OpenShift. Optional
     */
    BoomImpl(final GitHubRepository gitHubRepository, final OpenShiftProject openShiftProject, final GitHubWebhook webhook) {
        assert gitHubRepository != null : "gitHubRepository must be specified";
        assert openShiftProject != null : "openShiftProject must be specified";
        this.gitHubRepository = gitHubRepository;
        this.openShiftProject = openShiftProject;
        this.webhook = webhook;
    }

    private final GitHubRepository gitHubRepository;

    private final OpenShiftProject openShiftProject;

    private final GitHubWebhook webhook;

    @Override
    public GitHubRepository getCreatedRepository() {
        return this.gitHubRepository;
    }

    @Override
    public OpenShiftProject getCreatedProject() {
        return this.openShiftProject;
    }

    @Override
    public GitHubWebhook getGitHubWebhook() {
        return this.webhook;
    }
}
