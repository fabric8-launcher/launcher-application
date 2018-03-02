package io.fabric8.launcher.base.test.hoverfly;


import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.isHoverflyInSimulationMode;

/**
 * Gives a way:
 * - to have fixed environment only when Hoverfly is in simulation mode.
 * - to use the Launcher truststore
 */
public class LauncherHoverflyEnvironment extends ProvideSystemProperty {


    private final boolean simulationMode;

    private LauncherHoverflyEnvironment(String host, String port) {
        super("https.proxyHost", host);
        and("https.proxyPort", port);
        and("javax.net.ssl.trustStore", System.getenv("LAUNCHER_TESTS_TRUSTSTORE_PATH"));
        and("javax.net.ssl.trustStorePassword", "changeit");
        this.simulationMode = isHoverflyInSimulationMode();
    }

    public static LauncherHoverflyEnvironment createHoverflyEnvironment(String host, String port) {
        return new LauncherHoverflyEnvironment(host, port);
    }

    public static LauncherHoverflyEnvironment createDefaultHoverflyEnvironment() {
        return new LauncherHoverflyEnvironment("127.0.0.1", "8558");
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
