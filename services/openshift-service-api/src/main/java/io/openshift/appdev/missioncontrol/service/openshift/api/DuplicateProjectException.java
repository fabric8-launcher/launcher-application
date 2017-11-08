package io.openshift.appdev.missioncontrol.service.openshift.api;

/**
 * Indicates a project already exists
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class DuplicateProjectException extends RuntimeException {

    public DuplicateProjectException(final String projectDisplayName) {
        super(MSG_PREFIX + projectDisplayName);
        this.projectDisplayName = projectDisplayName;
    }

    private static final String MSG_PREFIX = "Project exists: ";

    private final String projectDisplayName;
}
