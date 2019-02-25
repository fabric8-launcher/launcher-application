package io.fabric8.launcher.web.producers;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProviderType;
import io.fabric8.launcher.service.git.spi.GitServiceConfigs;
import io.fabric8.launcher.service.git.spi.GitServiceFactories;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitServiceProducerTest {

    @Mock
    GitServiceFactories gitServiceFactories;

    @Mock
    GitServiceConfigs gitServiceConfigs;

    @Mock
    GitServiceConfig config;

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    IdentityProvider identityProvider;

    @Mock
    GitServiceFactory gitServiceFactory;

    @Test
    public void getGitServiceConfig_should_return_correct_factory_if_header_is_set() {
        // Return the given GitServiceFactory when asked
        when(mockRequest.getHeader(GitServiceProducer.GIT_PROVIDER_HEADER)).thenReturn("BitBucket");

        // Return a config on the given ID
        when(gitServiceConfigs.findById("BitBucket")).thenReturn(Optional.of(config));

        // Test it calls the BitBucket provider
        GitServiceProducer producer = new GitServiceProducer(gitServiceFactories, gitServiceConfigs);
        producer.getGitServiceConfig(mockRequest);

        // Verify that list is not called
        verify(gitServiceConfigs, never()).list();
        // Verify that findById was called once
        verify(gitServiceConfigs, times(1)).findById("BitBucket");
    }

    @Test
    public void getGitServiceConfig_should_return_default_factory_if_header_is_not_set() {
        when(mockRequest.getHeader(GitServiceProducer.GIT_PROVIDER_HEADER)).thenReturn(null);

        when(gitServiceConfigs.list()).thenReturn(singletonList(config));

        // Test it
        GitServiceProducer producer = new GitServiceProducer(gitServiceFactories, gitServiceConfigs);
        producer.getGitServiceConfig(mockRequest);

        // Verify that GitHub (default provider) was called once
        verify(gitServiceConfigs, times(1)).list();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGitServiceConfig_should_throw_exception_if_header_is_invalid() {
        // Return the given GitServiceFactory when asked
        when(mockRequest.getHeader(GitServiceProducer.GIT_PROVIDER_HEADER)).thenReturn("Blah");

        // Test it
        GitServiceProducer producer = new GitServiceProducer(gitServiceFactories, gitServiceConfigs);
        producer.getGitServiceConfig(mockRequest);
    }

    @Test
    public void getGitService_should_return_default_identity_if_header_is_not_specified() {
        TokenIdentity identity = TokenIdentity.of("authentication_token");
        String userName = "joe";

        when(mockRequest.getAttribute("USER_NAME")).thenReturn(userName);
        // Return the given GitServiceFactory when asked
        when(gitServiceFactory.getName()).thenReturn("Mocked");
        when(gitServiceFactory.getDefaultIdentity()).thenReturn(Optional.of(identity));
        when(gitServiceFactories.getGitServiceFactory(GitProviderType.GITHUB)).thenReturn(gitServiceFactory);

        when(config.getType()).thenReturn(GitProviderType.GITHUB);
        when(gitServiceConfigs.list()).thenReturn(singletonList(config));

        // Test it
        GitServiceProducer producer = new GitServiceProducer(gitServiceFactories, gitServiceConfigs);
        producer.getGitService(mockRequest, identityProvider, identity);

        verifyZeroInteractions(identityProvider);
        verify(gitServiceFactory).create(identity, userName, config);
        verify(gitServiceFactory, times(1)).getDefaultIdentity();
    }

    @Test
    public void getGitService_use_authorization_header() {
        TokenIdentity identity = TokenIdentity.of("default_authentication_token");
        TokenIdentity identityHeader = TokenIdentity.of("header_authentication_token");
        String userName = "joe";

        when(mockRequest.getAttribute("USER_NAME")).thenReturn(userName);
        when(mockRequest.getHeader(GitServiceProducer.GIT_AUTHORIZATION_HEADER)).thenReturn(identityHeader.toRequestAuthorization());

        // Return the given GitServiceFactory when asked
        when(gitServiceFactory.getName()).thenReturn("Mocked");
        when(gitServiceFactories.getGitServiceFactory(GitProviderType.GITHUB)).thenReturn(gitServiceFactory);

        when(config.getType()).thenReturn(GitProviderType.GITHUB);
        when(gitServiceConfigs.list()).thenReturn(singletonList(config));

        // Test it
        GitServiceProducer producer = new GitServiceProducer(gitServiceFactories, gitServiceConfigs);
        producer.getGitService(mockRequest, identityProvider, identity);

        verifyZeroInteractions(identityProvider);
        verify(gitServiceFactory).create(identityHeader, userName, config);
    }


}