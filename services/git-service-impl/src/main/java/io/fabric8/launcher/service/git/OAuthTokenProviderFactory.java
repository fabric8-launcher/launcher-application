package io.fabric8.launcher.service.git;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.service.git.api.GitServiceConfig;

@Singleton
public class OAuthTokenProviderFactory implements OAuthTokenProvider.Factory {

    @Inject
    HttpClient client;

    @Override
    public OAuthTokenProvider getProvider(GitServiceConfig config) {
        if ("GitLab".equalsIgnoreCase(config.getId())) {
            //return new GitLabOAuthProvider(client);
        }
        return new OAuthTokenProviderImpl(client);
    }
}
