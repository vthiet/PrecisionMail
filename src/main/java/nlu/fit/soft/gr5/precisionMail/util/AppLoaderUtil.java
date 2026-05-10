package nlu.fit.soft.gr5.precisionMail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppLoaderUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppLoaderUtil.class);
    private static Properties appConfig;

    static {
        appConfig = new Properties();

        try (InputStream is = AppLoaderUtil.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new RuntimeException("Cannot find application.properties in classpath");
            }

            LOGGER.info("application.properties loaded from classpath.");
            appConfig.load(is);
        } catch (IOException ex) {
            LOGGER.error("Failed to load application.properties.", ex);
            throw new RuntimeException("Failed to load application.properties", ex);
        }
    }

    public static String getProperty(String key) {
        return appConfig.getProperty(key);
    }

    public Properties getAppConfig() {
        return appConfig;
    }
}
