package io.fabric8.launcher.service.gitlab.api;

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
    WIKI
}
