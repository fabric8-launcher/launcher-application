package io.fabric8.launcher.service.openshift.impl;

import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.launcher.service.openshift.api.DuplicateProjectException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test to see if the ExceptionMapper works.
 */
public class ExceptionMapperTest {

    @Test
    public void shouldMapToDuplicateProjectException() {
        // given
        KubernetesClientException alreadyExists = createKubernetesClientException(409, "AlreadyExists");

        // when
        RuntimeException exception = ExceptionMapper.throwMappedException(alreadyExists, "projectName");

        // then
        assertEquals("Exception should have been right type", DuplicateProjectException.class, exception.getClass());
    }

    @Test
    public void shouldThrowRuntimeExceptionIfExceptionClassCanNotBeFound() {
        // given
        KubernetesClientException notKnown = createKubernetesClientException(300, "NotKnown");

        // when
        RuntimeException exception = ExceptionMapper.throwMappedException(notKnown, "projectName");

        // then
        assertEquals("Exception is not found original exception", KubernetesClientException.class, exception.getClass());
    }

    private KubernetesClientException createKubernetesClientException(int statusCode, String reason) {
        Status status = new Status();
        status.setReason("Some long description with a lot of text " + reason);
        return new KubernetesClientException(reason, statusCode, status);
    }
}
