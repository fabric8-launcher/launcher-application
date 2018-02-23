package io.fabric8.launcher.service.bitbucket.api;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BitbucketWebhookEvent {
    PULL_REQUEST_CREATED("pullrequest:created"),
    PULL_REQUEST_REJECTED("pullrequest:rejected"),
    PULL_REQUEST_FULFILLED("pullrequest:fulfilled"),
    PULL_REQUEST_UNAPPROVED("pullrequest:unapproved"),
    PULL_REQUEST_APPROVED("pullrequest:approved"),
    PULL_REQUEST_UPDATED("pullrequest:updated"),
    PULL_REQUEST_COMMENT_CREATED("pullrequest:comment_created"),
    PULL_REQUEST_COMMENT_UPDATED("pullrequest:comment_updated"),
    PULL_REQUEST_COMMENT_DELETED("pullrequest:comment_deleted"),
    ISSUE_CREATED("issue:created"),
    ISSUE_UPDATED("issue:updated"),
    ISSUE_COMMENT_CREATED("issue:comment_created"),
    REPO_CREATED("repo:created"),
    REPO_DELETED("repo:deleted"),
    REPO_IMPORTED("repo:imported"),
    REPO_FORK("repo:fork"),
    REPO_UPDATED("repo:updated"),
    REPO_PUSH("repo:push"),
    REPO_TRANSFER("repo:transfer"),
    REPO_COMMIT_COMMENT_CREATED("repo:commit_comment_created"),
    REPO_COMMIT_STATUS_CREATED("repo:commit_status_created"),
    REPO_COMMIT_STATUS_UPDATED("repo:commit_status_updated"),
    PROJECT_UPDATED("project:updated");


    private final String id;

    private static final Map<String, BitbucketWebhookEvent> EVENT_BY_ID = Stream.of(values())
            .collect(Collectors.toMap(BitbucketWebhookEvent::id, Function.identity()));

    BitbucketWebhookEvent(final String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String id() {
        return id;
    }

    public static BitbucketWebhookEvent resolve(final String id){
        return EVENT_BY_ID.get(id);
    }
}
