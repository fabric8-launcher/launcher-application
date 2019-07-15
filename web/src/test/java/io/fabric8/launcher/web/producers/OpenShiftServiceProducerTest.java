package io.fabric8.launcher.web.producers;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.ImmutableOpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.ImmutableParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OpenShiftServiceProducerTest {

    @Mock
    HttpServletRequest request;

    @Mock
    IdentityProvider identityProvider;

    @Mock
    OpenShiftServiceFactory factory;

    @Mock
    OpenShiftCluster cluster;

    @Mock
    OpenShiftClusterRegistry clusterRegistry;

    @Test
    public void should_authenticate_using_header_value() {
        when(clusterRegistry.findClusterById(IdentityProvider.ServiceType.OPENSHIFT)).thenReturn(Optional.of(cluster));
        when(request.getHeader(OpenShiftServiceProducer.OPENSHIFT_AUTHORIZATION_HEADER)).thenReturn("Bearer bar");
        TokenIdentity auth = TokenIdentity.of("foo");
        TokenIdentity authFromHeader = TokenIdentity.of("bar");
        OpenShiftServiceProducer producer = new OpenShiftServiceProducer(factory, clusterRegistry);
        producer.getOpenShiftService(request, identityProvider, auth);
        OpenShiftServiceFactory.Parameters parameters = ImmutableParameters.builder()
                .cluster(cluster)
                .identity(authFromHeader)
                .build();
        verifyZeroInteractions(identityProvider);
        verify(factory, times(1)).create(parameters);
    }

    @Test
    public void should_authenticate_using_identity_provider() {
        TokenIdentity auth = TokenIdentity.of("foo");
        when(clusterRegistry.findClusterById(IdentityProvider.ServiceType.OPENSHIFT)).thenReturn(Optional.of(cluster));
        when(request.getHeader(OpenShiftServiceProducer.OPENSHIFT_AUTHORIZATION_HEADER)).thenReturn(null);
        when(identityProvider.getIdentity(auth, IdentityProvider.ServiceType.OPENSHIFT)).thenReturn(Optional.of(auth));
        OpenShiftServiceProducer producer = new OpenShiftServiceProducer(factory, clusterRegistry);
        producer.getOpenShiftService(request, identityProvider, auth);
        OpenShiftServiceFactory.Parameters parameters = ImmutableParameters.builder()
                .cluster(cluster)
                .identity(auth)
                .build();
        verify(identityProvider, times(1)).getIdentity(auth, IdentityProvider.ServiceType.OPENSHIFT);
        verify(factory, times(1)).create(parameters);
    }

    @Test
    public void should_use_custom_url() {
        TokenIdentity auth = TokenIdentity.of("foo");
        when(request.getHeader(OpenShiftServiceProducer.OPENSHIFT_CLUSTER_URL_HEADER)).thenReturn("https://api.foo.com");
        when(request.getHeader(OpenShiftServiceProducer.OPENSHIFT_AUTHORIZATION_HEADER)).thenReturn("Bearer foo");
        OpenShiftServiceProducer producer = new OpenShiftServiceProducer(factory, clusterRegistry);
        OpenShiftService service = producer.getOpenShiftService(request, identityProvider, auth);
        OpenShiftServiceFactory.Parameters parameters = ImmutableParameters.builder()
                .cluster(ImmutableOpenShiftCluster.builder().id("custom").apiUrl("https://api.foo.com").build())
                .identity(auth)
                .build();
        verify(factory, times(1)).create(parameters);
    }

}
