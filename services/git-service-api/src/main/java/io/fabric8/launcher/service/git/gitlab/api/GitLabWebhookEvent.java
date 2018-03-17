package io.fabric8.launcher.service.git.gitlab.api;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public enum GitLabWebhookEvent {
    PUSH,
    ISSUES,
    MERGE_REQUESTS,
    TAG_PUSH,
    NOTE,
    JOB,
    PIPELINE,
    WIKI;

    public String id() {
        return name().toLowerCase();
    }
}
