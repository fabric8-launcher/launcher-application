package io.fabric8.launcher.core.api.events;

/**
 * All common status messages used in web sockets to inform clients
 * about the progress of the background operations
 */
public enum LauncherStatusEventKind implements StatusEventKind {

    GITHUB_CREATE("Creating your new GitHub repository"),
    GITHUB_PUSHED("Pushing your customized Booster code into the repo"),
    OPENSHIFT_CREATE("Creating your project on OpenShift"),
    OPENSHIFT_PIPELINE("Setting up your build pipeline"),
    GITHUB_WEBHOOK("Configuring to trigger builds on Git pushes");

    LauncherStatusEventKind(String message) {
        this.message = message;
    }

    private final String message;

    @Override
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
