package com.apiframework.base;

import com.apiframework.config.ConfigManager;
import com.apiframework.utils.AuthManager;
import com.apiframework.utils.ReportManager;
import com.apiframework.utils.RequestResponseLogger;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;

/**
 * Base class for all API test classes.
 * Sets up RestAssured defaults, logging filter, and ExtentReports integration.
 */
public class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected static RequestSpecification requestSpec;
    protected static ConfigManager config;
    protected static AuthManager authManager;
    protected static ReportManager reportManager;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        config = ConfigManager.getInstance();
        authManager = AuthManager.getInstance();
        reportManager = ReportManager.getInstance();

        log.info("=== Test Suite Starting | Environment: {} | Base URL: {} ===",
                config.getActiveEnv(), config.getBaseUrl());

        RestAssured.baseURI = config.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestResponseLogger())
                .build();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeEachTest(Method method) {
        String testName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        log.info("--- Starting test: [{}.{}] ---", className, testName);
        reportManager.createTest(className + " :: " + testName);
    }

    @AfterMethod(alwaysRun = true)
    public void afterEachTest(ITestResult result) {
        ExtentTest test = reportManager.getCurrentTest();
        if (test != null) {
            switch (result.getStatus()) {
                case ITestResult.SUCCESS:
                    test.log(Status.PASS, "Test PASSED");
                    break;
                case ITestResult.FAILURE:
                    test.log(Status.FAIL, "Test FAILED: " + result.getThrowable().getMessage());
                    test.log(Status.FAIL, result.getThrowable());
                    break;
                case ITestResult.SKIP:
                    test.log(Status.SKIP, "Test SKIPPED: "
                            + (result.getThrowable() != null ? result.getThrowable().getMessage() : ""));
                    break;
                default:
                    break;
            }
        }
        log.info("--- Finished test: [{}] Status: {} ---",
                result.getName(), statusName(result.getStatus()));
    }

    @AfterSuite(alwaysRun = true)
    public void globalTeardown() {
        reportManager.flush();
        log.info("=== Test Suite Finished. Report generated at: {} ===", config.getReportPath());
    }

    /** Returns a RequestSpecification pre-configured with a Bearer token. */
    protected RequestSpecification authenticatedRequest() {
        return RestAssured.given()
                .spec(requestSpec)
                .header("Authorization", authManager.getBearerToken());
    }

    /** Returns a RequestSpecification without authentication. */
    protected RequestSpecification unauthenticatedRequest() {
        return RestAssured.given().spec(requestSpec);
    }

    /** Logs request/response to the ExtentReport node. */
    protected void logToReport(String method, String url, String requestBody, Response response) {
        reportManager.logRequest(method, url, requestBody != null ? requestBody : "");
        reportManager.logResponse(response.getStatusCode(), response.getBody().asPrettyString());
    }

    private String statusName(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASS";
            case ITestResult.FAILURE: return "FAIL";
            case ITestResult.SKIP:    return "SKIP";
            default:                  return "UNKNOWN";
        }
    }
}
