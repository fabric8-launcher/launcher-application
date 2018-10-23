package io.fabric8.launcher.service.git.gitea.api;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public enum GiteaWebhookEvent {

    CREATE,
    DELETE,
    FORK,
    PUSH,
    ISSUES,
    ISSUE_COMMENT,
    PULL_REQUEST,
    REPOSITORY,
    RELEASE;

    public String id() {
        return name().toLowerCase();
    }
}
