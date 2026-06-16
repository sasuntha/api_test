package com.apiframework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager that supports environment switching.
 * Active environment is resolved from: -Denv=<qa|dev> (defaults to "qa").
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties props = new Properties();
    private final String activeEnv;

    private ConfigManager() {
        activeEnv = resolveEnv();
        loadProperties();
        log.info("ConfigManager initialized for environment: [{}]", activeEnv);
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private String resolveEnv() {
        String envFromSystem = System.getProperty("env");
        if (envFromSystem != null && !envFromSystem.isBlank()) {
            return envFromSystem.trim().toLowerCase();
        }
        // Fall back to config.properties default
        Properties tmp = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/config.properties")) {
            if (is != null) tmp.load(is);
        } catch (IOException e) {
            log.warn("Could not read config.properties for env resolution, using 'qa'");
        }
        return tmp.getProperty("env", "qa");
    }

    private void loadProperties() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/config.properties")) {
            if (is == null) throw new RuntimeException("config/config.properties not found on classpath");
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    /** Returns a property value prefixed by active environment, e.g. "qa.base.url". */
    public String get(String key) {
        String envKey = activeEnv + "." + key;
        String value = props.getProperty(envKey);
        if (value == null) {
            value = props.getProperty(key);
        }
        if (value == null) {
            throw new RuntimeException("Missing config key: " + envKey);
        }
        return value.trim();
    }

    public String getBaseUrl()  { return get("base.url"); }
    public String getUsername() { return get("username"); }
    public String getPassword() { return get("password"); }
    public int    getTimeout()  { return Integer.parseInt(get("timeout")); }
    public String getActiveEnv() { return activeEnv; }

    public String getReportPath()  { return props.getProperty("report.output.path", "test-output/ExtentReports/APITestReport.html"); }
    public String getReportTitle() { return props.getProperty("report.title", "API Test Report"); }
    public String getReportName()  { return props.getProperty("report.name",  "API Test Execution Report"); }
}
