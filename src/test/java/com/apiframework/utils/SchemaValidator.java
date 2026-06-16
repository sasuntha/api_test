package com.apiframework.utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Utility class for validating API responses against JSON schemas
 * stored in src/test/resources/schemas/.
 */
public class SchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);
    private static final String SCHEMA_DIR = "schemas/";

    private SchemaValidator() {}

    /**
     * Validates a response body against the named JSON schema file.
     *
     * @param response       the REST Assured Response to validate
     * @param schemaFileName the schema file name (e.g. "user_schema.json")
     */
    public static void validate(Response response, String schemaFileName) {
        String schemaPath = SCHEMA_DIR + schemaFileName;
        log.info("Validating response against schema: {}", schemaPath);

        InputStream schema = SchemaValidator.class.getClassLoader().getResourceAsStream(schemaPath);
        if (schema == null) {
            throw new RuntimeException("Schema file not found on classpath: " + schemaPath);
        }

        assertThat("JSON schema validation failed for: " + schemaFileName,
                response.getBody().asString(),
                JsonSchemaValidator.matchesJsonSchema(schema));

        log.info("Schema validation passed: {}", schemaFileName);
    }

    /**
     * Validates a JSON string directly against the named schema.
     */
    public static void validate(String jsonBody, String schemaFileName) {
        String schemaPath = SCHEMA_DIR + schemaFileName;
        log.info("Validating JSON body against schema: {}", schemaPath);

        InputStream schema = SchemaValidator.class.getClassLoader().getResourceAsStream(schemaPath);
        if (schema == null) {
            throw new RuntimeException("Schema file not found on classpath: " + schemaPath);
        }

        assertThat("JSON schema validation failed for: " + schemaFileName,
                jsonBody,
                JsonSchemaValidator.matchesJsonSchema(schema));

        log.info("Schema validation passed: {}", schemaFileName);
    }
}
