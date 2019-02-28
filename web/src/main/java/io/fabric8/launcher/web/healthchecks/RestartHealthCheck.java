package io.fabric8.launcher.web.healthchecks;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class RestartHealthCheck implements HealthCheck {

    private boolean restart;

    public void restart() {
        this.restart = true;
    }

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.builder().name("restart").state(!restart).build();
    }
}
