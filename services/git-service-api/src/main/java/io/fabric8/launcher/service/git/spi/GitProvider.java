package io.fabric8.launcher.service.git.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import io.fabric8.launcher.base.EnvironmentEnum;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, PARAMETER, METHOD})
public @interface GitProvider {

    enum GitProviderEnvironment implements EnvironmentEnum {
        LAUNCHER_BACKEND_GIT_PROVIDER
    }

    enum GitProviderType {
        GITHUB,
        GITLAB,
        BITBUCKET,
        GITEA
    }

    GitProviderType value();

    final class GitProviderLiteral extends AnnotationLiteral<GitProvider> implements GitProvider {

        private static final long serialVersionUID = 1L;

        private final GitProviderType value;

        public static GitProviderLiteral of(GitProviderType value) {
            return new GitProviderLiteral(value);
        }

        @Override
        public GitProviderType value() {
            return value;
        }

        private GitProviderLiteral(GitProviderType value) {
            this.value = value;
        }

    }
}
