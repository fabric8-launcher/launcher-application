package io.fabric8.launcher.base.test.hoverfly;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.isHoverflyInSimulationMode;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Gives a way:
 * - to have fixed environment only when Hoverfly is in simulation mode.
 * - to use the Launcher truststore
 */
public class LauncherHoverflyEnvironment extends ProvideSystemProperty {


    private final String host;
    private final String port;
    private final boolean simulationMode;
    private Path trustStoreTempFilePath;

    private LauncherHoverflyEnvironment(String host, String port) {
        this.host = host;
        this.port = port;
        this.simulationMode = isHoverflyInSimulationMode();
    }

    public static LauncherHoverflyEnvironment createHoverflyEnvironment(String host, String port) {
        return new LauncherHoverflyEnvironment(host, port);
    }

    public static LauncherHoverflyEnvironment createDefaultHoverflyEnvironment() {
        return new LauncherHoverflyEnvironment("127.0.0.1", "8558");
    }

    @Override
    protected void before() throws Throwable {
        initTrustStore();
        and("https.proxyHost", host);
        and("https.proxyPort", port);
        and("javax.net.ssl.trustStore", trustStoreTempFilePath.toAbsolutePath().toString());
        and("javax.net.ssl.trustStorePassword", "changeit");

        super.before();
    }

    @Override
    protected void after() {
        super.after();
        deleteTrusStoreTempFolder();
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

    private void initTrustStore() throws IOException {
        trustStoreTempFilePath = Files.createTempFile("hoverfly", ".jks");
        try(final InputStream trustStoreInputStream = LauncherHoverflyEnvironment.class.getResourceAsStream("/hoverfly/hoverfly.jks")) {
            Files.copy(trustStoreInputStream, trustStoreTempFilePath, REPLACE_EXISTING);
        }
    }

    private void deleteTrusStoreTempFolder() {
        try {
            Files.deleteIfExists(trustStoreTempFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
