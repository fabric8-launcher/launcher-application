package io.fabric8.launcher.web.healthchecks;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RestartHealthCheckTest {

    @Test
    public void should_be_down_when_restarted() {
        RestartHealthCheck check = new RestartHealthCheck();
        assertThat(check.call().getState()).isEqualTo(HealthCheckResponse.State.UP);
        check.restart();
        assertThat(check.call().getState()).isEqualTo(HealthCheckResponse.State.DOWN);

    }

}