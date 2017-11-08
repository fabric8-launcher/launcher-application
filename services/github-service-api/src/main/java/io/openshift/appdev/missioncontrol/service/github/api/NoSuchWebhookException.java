package io.openshift.appdev.missioncontrol.service.github.api;

import java.net.URL;

/**
 * Indicates a webhook was requested to be obtained, but it does not exist
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class NoSuchWebhookException extends RuntimeException {

    // Use factory method
    private NoSuchWebhookException(final GitHubRepository repo, final URL url) throws IllegalArgumentException {
        super("Could not find webhook " + url.toString()
                      + " for repository " + repo.getFullName());
    }

    /**
     * @param repo The repository containing the webhook we attempted to retrieve
     * @param url  The URL of the webhook that could not be created because one
     *             equal by value to it exists
     * @throws IllegalArgumentException If either argument is not specified
     */
    public static NoSuchWebhookException create(final GitHubRepository repo, final URL url) throws IllegalArgumentException {
        if (repo == null) {
            throw new IllegalArgumentException("repo is required");
        }
        if (url == null) {
            throw new IllegalArgumentException("url is required");
        }
        final NoSuchWebhookException dpe = new NoSuchWebhookException(repo, url);
        return dpe;
    }
}
