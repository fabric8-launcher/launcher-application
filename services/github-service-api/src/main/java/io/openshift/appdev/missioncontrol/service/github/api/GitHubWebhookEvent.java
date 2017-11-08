package io.openshift.appdev.missioncontrol.service.github.api;

public enum GitHubWebhookEvent {
    COMMIT_COMMENT,
    CREATE,
    DELETE,
    DEPLOYMENT,
    DEPLOYMENT_STATUS,
    DOWNLOAD,
    FOLLOW,
    FORK,
    FORK_APPLY,
    GIST,
    GOLLUM,
    ISSUE_COMMENT,
    ISSUES,
    MEMBER,
    PAGE_BUILD,
    PUBLIC,
    PULL_REQUEST,
    PULL_REQUEST_REVIEW_COMMENT,
    PUSH,
    RELEASE,
    REPOSITORY, // only valid for org hooks
    STATUS,
    TEAM_ADD,
    WATCH,
    PING,
    /**
     * Special event type that means "every possible event"
     */
    ALL;

}
