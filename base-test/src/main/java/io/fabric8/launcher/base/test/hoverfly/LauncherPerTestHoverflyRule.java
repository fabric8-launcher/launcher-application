package io.fabric8.launcher.base.test.hoverfly;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static io.specto.hoverfly.junit.core.SimulationSource.defaultPath;

public class LauncherPerTestHoverflyRule implements TestRule {

    private static final Logger log = Logger.getLogger(LauncherPerTestHoverflyRule.class.getName());

    private final HoverflyRule hoverflyRule;

    public LauncherPerTestHoverflyRule(HoverflyRule hoverflyRule) {
        this.hoverflyRule = hoverflyRule;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (HoverflyMode.CAPTURE == hoverflyRule.getHoverflyMode()) {
                    hoverflyRule.capture("captured/" + getPerTestMethodSimulation(description));
                } else {
                    hoverflyRule.simulate(getSimulationPath(description));
                }
                base.evaluate();
            }
        };
    }

    private SimulationSource getSimulationPath(Description description) {
        try {
            final SimulationSource simulationSource = defaultPath(getPerTestMethodSimulation(description));
            simulationSource.getSimulation(); // to check if it can be loaded - PR to hoverfly to expose check instead would make it nicer
            return simulationSource;
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load test-specific simulation, trying to load shared per-class simulation", e);
            return defaultPath(getPerTestClassSimulation(description));
        }
    }

    private String getPerTestMethodSimulation(Description description) {
        return description.getTestClass().getSimpleName().toLowerCase() + File.separator + description.getMethodName().toLowerCase() + ".json";
    }

    private String getPerTestClassSimulation(Description description) {
        return description.getTestClass().getSimpleName().toLowerCase() + File.separator + "shared.json";
    }

}

