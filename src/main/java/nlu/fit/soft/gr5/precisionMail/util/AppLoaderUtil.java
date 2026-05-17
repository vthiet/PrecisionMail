package nlu.fit.soft.gr5.precisionMail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppLoaderUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppLoaderUtil.class);
    private static final String CONFIG_FILE = "application.properties";
    private static final String EXAMPLE_CONFIG_FILE = "example.application.properties";
    private static Properties appConfig;

    static {
        appConfig = new Properties();

        try (InputStream is = openConfigStream()) {

            if (is == null) {
                LOGGER.warn("No application configuration file found in classpath.");
            } else {
                appConfig.load(is);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to load application configuration.", ex);
            throw new RuntimeException("Failed to load application configuration", ex);
        }
    }

    private static InputStream openConfigStream() {
        InputStream is = AppLoaderUtil.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE);

        if (is != null) {
            LOGGER.info("{} loaded from classpath.", CONFIG_FILE);
            return is;
        }

        InputStream example = AppLoaderUtil.class
                .getClassLoader()
                .getResourceAsStream(EXAMPLE_CONFIG_FILE);
        if (example != null) {
            LOGGER.warn("{} not found. Falling back to {}.", CONFIG_FILE, EXAMPLE_CONFIG_FILE);
        }
        return example;
    }

    public static String getProperty(String key) {
        return appConfig.getProperty(key);
    }

    public Properties getAppConfig() {
        return appConfig;
    }
}
