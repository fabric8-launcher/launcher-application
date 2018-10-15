package io.fabric8.launcher.web.producers;

import java.util.Objects;

import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.Application;

import static io.fabric8.launcher.core.spi.Application.ApplicationLiteral.of;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.valueOf;

/**
 * Looks up implementation of the CDI bean based on X-App header passed as part of request.
 * If not present defaults to "launcher"
 *
 * @see Application.ApplicationType
 *
 * @param <T> type of the bean to look up
 */
abstract class ApplicationTypeBasedProducer<T> {

    private static final String HEADER = "X-App";

    private static final String DEFAULT_APP = "launcher";

    T extractAppBasedImplementation(HttpServletRequest request, Instance<T> instances, Class<T> cls) {
        // If X-App is not specified, assume fabric8-launcher
        final String app = Objects.toString(request.getHeader(HEADER), DEFAULT_APP).toUpperCase();
        final Application.ApplicationType type;
        try {
            type = valueOf(app);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Header 'X-App' has an invalid value: " + app);
        }
        final Instance<T> instance = instances.select(cls, of(type));
        if (instance.isUnsatisfied()) {
            throw new IllegalArgumentException("Implementation of " + cls.getName() + " not found for app:" + app);
        }
        return instance.get();
    }
}
