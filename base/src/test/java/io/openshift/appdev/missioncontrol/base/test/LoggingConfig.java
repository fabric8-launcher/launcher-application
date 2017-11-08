package io.openshift.appdev.missioncontrol.base.test;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * A logging configuration class that first attempts to load a logging.properties file from the classpath, and then falls
 * back to a programmatic configuration. To enable, specify -Djava.util.logging.config.class=LoggingConfig
 * in test app configuration.
 */
public class LoggingConfig {
    public LoggingConfig() {
        try {
            // Load a properties file from class path java.util.logging.config.file
            final LogManager logManager = LogManager.getLogManager();
            URL configURL = getClass().getResource("/logging.properties");
            if (configURL != null) {
                try (InputStream is = configURL.openStream()) {
                    logManager.readConfiguration(is);
                }
            } else {
                // Programmatic configuration
                System.setProperty("java.util.logging.SimpleFormatter.format",
                                   "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s [%3$s] %5$s %6$s%n");

                final ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(Level.FINEST);
                consoleHandler.setFormatter(new SimpleFormatter());

                final Logger app = Logger.getLogger("app");
                app.setLevel(Level.FINEST);
                app.addHandler(consoleHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
