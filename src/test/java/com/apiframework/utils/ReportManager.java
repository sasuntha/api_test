package com.apiframework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.apiframework.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages the ExtentReports lifecycle: initialization, test node creation,
 * request/response attachment, and report flushing.
 */
public class ReportManager {

    private static final Logger log = LoggerFactory.getLogger(ReportManager.class);
    private static ReportManager instance;
    private ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    private ReportManager() {
        init();
    }

    public static synchronized ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }

    private void init() {
        ConfigManager config = ConfigManager.getInstance();
        String reportPath = config.getReportPath();

        // Ensure parent directories exist
        new File(reportPath).getParentFile().mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle(config.getReportTitle());
        spark.config().setReportName(config.getReportName());
        spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
        spark.config().setEncoding("UTF-8");

        extentReports = new ExtentReports();
        extentReports.attachReporter(spark);
        extentReports.setSystemInfo("Environment", config.getActiveEnv().toUpperCase());
        extentReports.setSystemInfo("Base URL",    config.getBaseUrl());
        extentReports.setSystemInfo("Executed At", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        extentReports.setSystemInfo("Tester", System.getProperty("user.name", "Automation"));

        log.info("ExtentReports initialized. Report path: {}", reportPath);
    }

    public ExtentTest createTest(String testName, String description) {
        ExtentTest test = extentReports.createTest(testName, description);
        currentTest.set(test);
        return test;
    }

    public ExtentTest createTest(String testName) {
        return createTest(testName, "");
    }

    public ExtentTest getCurrentTest() {
        return currentTest.get();
    }

    /** Logs a request details block to the current test node. */
    public void logRequest(String method, String url, String body) {
        ExtentTest test = getCurrentTest();
        if (test == null) return;
        String html = "<details><summary><b>Request: " + method + " " + url + "</b></summary>"
                + "<pre>" + escapeHtml(body) + "</pre></details>";
        test.info(html);
    }

    /** Logs a response details block to the current test node. */
    public void logResponse(int statusCode, String body) {
        ExtentTest test = getCurrentTest();
        if (test == null) return;
        String color = (statusCode >= 200 && statusCode < 300) ? "green" : "red";
        String html = "<details><summary><b>Response: <span style='color:" + color + "'>"
                + statusCode + "</span></b></summary>"
                + "<pre>" + escapeHtml(body) + "</pre></details>";
        test.info(html);
    }

    public void flush() {
        if (extentReports != null) {
            extentReports.flush();
            log.info("ExtentReports flushed.");
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
