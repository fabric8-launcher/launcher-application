package io.fabric8.launcher.service.gitlab.impl;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public enum GitLabWebHookEvent {
    PUSH,
    ISSUES,
    MERGE_REQUESTS,
    TAG_PUSH,
    NOTE,
    JOB,
    PIPELINE,
    WIKI
}
