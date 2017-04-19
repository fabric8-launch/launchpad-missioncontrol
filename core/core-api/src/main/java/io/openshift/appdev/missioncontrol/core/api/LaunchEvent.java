package io.openshift.appdev.missioncontrol.core.api;

import java.util.UUID;

/**
 * Launch event with all the information necessary for tracking
 */
public class LaunchEvent {
    private String user;
    private UUID id;
    private String githubRepo;
    private String openshiftProjectName;

    public LaunchEvent(String user, UUID id, String githubRepo, String openshiftProjectName) {
        super();
        this.user = user;
        this.id = id;
        this.githubRepo = githubRepo;
        this.openshiftProjectName = openshiftProjectName;
    }

    public String getUser() {
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public String getOpenshiftProjectName() {
        return openshiftProjectName;
    }

}
