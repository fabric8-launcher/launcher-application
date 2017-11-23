package io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.launcher.service.openshift.api.DuplicateProjectException;
import io.fabric8.launcher.service.openshift.api.QuotaExceedException;

/**
 * Util class that 'maps' KubernetesClientException to domain specific RuntimeException.
 */
public class ExceptionMapper {

    /**
     * For given KubernetesClientException map it to a domain specific runtime Exception,
     * if none is found return original KubernetesClientException
     *
     * @param kce     the KubernetesClientException to find a domain specific alternative for
     * @param message is used to create the domain specific exception with
     * @return the domain specific exception or the original if none is found
     */
    public static RuntimeException throwMappedException(KubernetesClientException kce, String message) {
        for (ExceptionMapping exceptionMapping : ExceptionMapping.values()) {
            if (exceptionMapping.isMatchingException(kce)) {
                return exceptionMapping.createInstance(message, kce);
            }
        }

        return kce;
    }

    private enum ExceptionMapping {
        DUPLICATE(409, "AlreadyExists") {
            @Override
            RuntimeException createInstance(String message, Throwable reason) {
                return new DuplicateProjectException(message, reason);
            }
        },
        QUOTA(403, "cannot create more") {
            @Override
            RuntimeException createInstance(String message, Throwable reason) {
                return new QuotaExceedException(message, reason);
            }
        };

        ExceptionMapping(int statusCode, String statusReason) {
            this.statusCode = statusCode;
            this.statusReason = statusReason;
        }

        private final int statusCode;

        private final String statusReason;

        abstract RuntimeException createInstance(String message, Throwable reason);

        private boolean isMatchingException(KubernetesClientException kce) {
            return kce.getCode() == statusCode && kce.getStatus().getReason().contains(statusReason);
        }
    }
}
