package io.fabric8.launcher.service.hoverfly;

import static io.fabric8.launcher.service.hoverfly.HoverflyRuleConfigurer.isHoverflyInSimulationMode;

import org.junit.contrib.java.lang.system.ProvideSystemProperty;

/**
 * Gives a way to have fixed environment only when Hoverfly is in simulation mode.
 */
public class HoverflySimulationEnvironment extends ProvideSystemProperty {

    private boolean simulationMode;

    @Override
    protected void before() throws Throwable {
        simulationMode = isHoverflyInSimulationMode();
        if (simulationMode) {
            super.before();
        }
    }

    @Override
    protected void after() {
        if (simulationMode) {
            super.after();
        }
    }

    @Override
    public HoverflySimulationEnvironment and(final String name, final String value) {
        return (HoverflySimulationEnvironment) super.and(name, value);
    }
}
