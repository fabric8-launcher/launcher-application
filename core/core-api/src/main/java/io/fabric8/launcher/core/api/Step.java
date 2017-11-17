package io.fabric8.launcher.core.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * CDI qualifier for step number
 */
@Qualifier
@Retention(RUNTIME)
@Target({PARAMETER})
public @interface Step {
    StatusEventType value();

    public static final class Literal extends AnnotationLiteral<Step> implements Step {
        private static final long serialVersionUID = 1L;

        private final StatusEventType statusEventType;

        public Literal(StatusEventType statusEventType) {
            this.statusEventType = statusEventType;
        }

        @Override
        public StatusEventType value() {
            return statusEventType;
        }
    }
}
