package nlu.fit.soft.gr5.precisionMail.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppLoaderUtil {
    private static Properties appConfig;

    static {
        appConfig = new Properties();

        try (InputStream is = AppLoaderUtil.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new RuntimeException("Cannot find application.properties in classpath");
            }

            System.out.println(
                    AppLoaderUtil.class.getClassLoader()
                            .getResource("application.properties")
            );
            appConfig.load(is);

        } catch (IOException ex) {
            throw new RuntimeException("Failed toLst load application.properties", ex);
        }
    }

    public static String getProperty(String key) {
        return appConfig.getProperty(key);
    }

    public Properties getAppConfig(){
        return  appConfig;
    }
}
