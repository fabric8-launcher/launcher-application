package io.fabric8.launcher.base.test.hoverfly;


import java.util.logging.Logger;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.isHoverflyInSimulationMode;

/**
 * Gives a way to have fixed environment only when Hoverfly is in simulation mode.
 */
public class LauncherHoverflyEnvironment extends ProvideSystemProperty {
    private static final Logger LOG = Logger.getLogger(LauncherHoverflyEnvironment.class.getName());

    private final boolean simulationMode;

    private LauncherHoverflyEnvironment(String host, String port) {
        super("https.proxyHost", host);
        and("https.proxyPort", port);
        this.simulationMode = isHoverflyInSimulationMode();
    }

    public static LauncherHoverflyEnvironment createHoverflyEnvironment(String host, String port) {
        return new LauncherHoverflyEnvironment(host, port);
    }

    public static LauncherHoverflyEnvironment createDefaultHoverflyEnvironment(final HoverflyRule hoverflyRule) {
        return new LauncherHoverflyEnvironment("127.0.0.1", String.valueOf(hoverflyRule.getProxyPort()));
    }

    public LauncherHoverflyEnvironment andForSimulationOnly(final String name, final String value) {
        if (simulationMode) {
            super.and(name, value);
        }
        return this;
    }

    @Override
    public LauncherHoverflyEnvironment and(final String name, final String value) {
        super.and(name, value);
        return this;
    }
}
