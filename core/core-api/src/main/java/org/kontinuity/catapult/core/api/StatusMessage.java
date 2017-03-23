package org.kontinuity.catapult.core.api;

/**
 * That holds all status messages that we send to the clients via websockets to inform them about the status of their project
 */
public enum StatusMessage {

    GITHUB_CREATE("created github repository"),
    GITHUB_PUSHED("pushed code to github repository"),
    GITHUB_FORK("forked github repository"),
    GITHUB_WEBHOOK("added webhook to github repository"),
    OPENSHIFT_CREATE("openshift project created"),
    OPENSHIFT_PIPELINE("openshift added pipeline to project");

    StatusMessage(String message) {
        this.message = message;
    }

    private final String message;

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
