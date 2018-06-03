package io.fabric8.launcher.base.test.hoverfly;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.isHoverflyInSimulationMode;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Gives a way:
 * - to have fixed environment only when Hoverfly is in simulation mode.
 * - to use the Launcher truststore
 */
public class LauncherHoverflyEnvironment extends ProvideSystemProperty {
    private static final Logger logger = Logger.getLogger(LauncherHoverflyEnvironment.class.getName());

    private static final String DEFAULT_HOST_NAME = System.getenv().getOrDefault("LAUNCHER_HOVERFLY_HOST", "127.0.0.1");

    private final boolean simulationMode;

    private Path trustStoreTempFilePath;

    private LauncherHoverflyEnvironment(String host, String port) {
        super("https.proxyHost", host);
        and("https.proxyPort", port);
        this.simulationMode = isHoverflyInSimulationMode();
    }

    public static LauncherHoverflyEnvironment createHoverflyEnvironment(String host, String port) {
        return new LauncherHoverflyEnvironment(host, port);
    }

    public static LauncherHoverflyEnvironment createDefaultHoverflyEnvironment(final HoverflyRule hoverflyRule) {
        return new LauncherHoverflyEnvironment(DEFAULT_HOST_NAME, String.valueOf(hoverflyRule.getProxyPort()));
    }

    public LauncherHoverflyEnvironment andForSimulationOnly(final String name, final String value) {
        if (simulationMode) {
            super.and(name, value);
        }
        return this;
    }

    @Override
    protected void before() throws Throwable {
        initTrustStore();
        String trustorePath = trustStoreTempFilePath.toAbsolutePath().toString();
        logger.info("Setting trustStore path to: " + trustorePath);
        and("javax.net.ssl.trustStore", trustorePath);
        and("javax.net.ssl.trustStorePassword", "changeit");
        super.before();
    }

    @Override
    protected void after() {
        super.after();
        deleteTrustStoreTempFolder();
    }

    @Override
    public LauncherHoverflyEnvironment and(final String name, final String value) {
        super.and(name, value);
        return this;
    }

    private void initTrustStore() throws IOException {
        trustStoreTempFilePath = Files.createTempFile("hoverfly", ".jks");
        try (final InputStream trustStoreInputStream = LauncherHoverflyEnvironment.class.getResourceAsStream("/hoverfly/hoverfly.jks")) {
            Files.copy(trustStoreInputStream, trustStoreTempFilePath, REPLACE_EXISTING);
        }
    }

    private void deleteTrustStoreTempFolder() {
        try {
            Files.deleteIfExists(trustStoreTempFilePath);
        } catch (final IOException e) {
            logger.log(Level.WARNING, "Error while deleting " + trustStoreTempFilePath, e);
        }
    }
}