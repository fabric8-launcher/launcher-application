package io.fabric8.launcher.service.git.github.api;

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
    INSTALLATION,
    INSTALLATION_REPOSITORIES,
    ISSUE_COMMENT,
    ISSUES,
    LABEL,
    MARKETPLACE_PURCHASE,
    MEMBER,
    MEMBERSHIP,
    MILESTONE,
    ORGANIZATION,
    ORG_BLOCK,
    PAGE_BUILD,
    PROJECT_CARD,
    PROJECT_COLUMN,
    PROJECT,
    PUBLIC,
    PULL_REQUEST,
    PULL_REQUEST_REVIEW,
    PULL_REQUEST_REVIEW_COMMENT,
    PUSH,
    RELEASE,
    REPOSITORY, // only valid for org hooks
    STATUS,
    TEAM,
    TEAM_ADD,
    WATCH,
    PING,
    /**
     * Special event type that means "every possible event"
     */
    ALL;


    public String id() {
        return name().toLowerCase();
    }
}
