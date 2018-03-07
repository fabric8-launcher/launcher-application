package io.fabric8.launcher.service.git.api;

/**
 * Indicates a specified organization does not exist.
 *
 */
public class NoSuchOrganizationException extends RuntimeException {

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public NoSuchOrganizationException(final String message) {
        super(message);
    }

    /**
     * version number of this serializable class.
     */
    private static final long serialVersionUID = -78123136358122472L;
}
