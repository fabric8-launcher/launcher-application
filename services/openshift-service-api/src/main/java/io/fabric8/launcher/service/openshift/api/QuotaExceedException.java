package io.fabric8.launcher.service.openshift.api;

/**
 * Indicates that the project could not be create due to insufficient quota.
 */
public class QuotaExceedException extends RuntimeException {

    public QuotaExceedException(final String projectName, Throwable reason) {
        super(String.format("Could not create project: '%s' due to insufficient quota", projectName), reason);
    }
}
