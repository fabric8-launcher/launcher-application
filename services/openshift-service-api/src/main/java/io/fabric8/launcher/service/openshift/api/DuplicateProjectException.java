package io.fabric8.launcher.service.openshift.api;

/**
 * Indicates a project already exists
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class DuplicateProjectException extends RuntimeException {

    public DuplicateProjectException(final String projectDisplayName, Throwable reason) {
        super(MSG_PREFIX + projectDisplayName, reason);
    }

    private static final String MSG_PREFIX = "Project exists: ";
}
