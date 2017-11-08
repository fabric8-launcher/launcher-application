package io.openshift.appdev.missioncontrol.service.github.api;

import java.net.URL;

/**
 * Indicates a webhook was requested to be created, but it already exists
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class DuplicateWebhookException extends RuntimeException {

    // Use factory method
    private DuplicateWebhookException(final URL url) throws IllegalArgumentException {
        super("Could not create webhook as it already exists: " + url.toString());
    }

    /**
     * @param url The URL of the webhook that could not be created because one
     *            equal by value to it exists
     * @throws IllegalArgumentException If the webhook is not specified
     */
    public static DuplicateWebhookException create(final URL url) throws IllegalArgumentException {
        if (url == null) {
            throw new IllegalArgumentException("url is required");
        }
        final DuplicateWebhookException dpe = new DuplicateWebhookException(url);
        return dpe;
    }
}
