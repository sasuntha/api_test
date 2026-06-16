package com.apiframework.utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Rest Assured filter that logs every HTTP request and response
 * to SLF4J in a structured, readable format.
 */
public class RequestResponseLogger implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLogger.class);
    private static final String SEPARATOR = "─".repeat(70);

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {

        logRequest(requestSpec);
        long startMs = System.currentTimeMillis();
        Response response = ctx.next(requestSpec, responseSpec);
        long elapsed = System.currentTimeMillis() - startMs;
        logResponse(response, elapsed);
        return response;
    }

    private void logRequest(FilterableRequestSpecification req) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(SEPARATOR);
        sb.append("\n  REQUEST");
        sb.append("\n  ").append(req.getMethod()).append("  ").append(req.getURI());

        if (req.getHeaders() != null && req.getHeaders().exist()) {
            sb.append("\n  Headers:");
            req.getHeaders().forEach(h -> {
                // Mask Authorization header value
                String value = h.getName().equalsIgnoreCase("Authorization")
                        ? maskToken(h.getValue()) : h.getValue();
                sb.append("\n    ").append(h.getName()).append(": ").append(value);
            });
        }

        if (req.getBody() != null) {
            sb.append("\n  Body:\n    ").append(req.getBody().toString());
        }

        sb.append("\n").append(SEPARATOR);
        log.info(sb.toString());
    }

    private void logResponse(Response response, long elapsedMs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(SEPARATOR);
        sb.append("\n  RESPONSE  [").append(elapsedMs).append(" ms]");
        sb.append("\n  Status: ").append(response.getStatusCode())
          .append(" ").append(response.getStatusLine());

        if (response.getHeaders() != null) {
            sb.append("\n  Headers:");
            response.getHeaders().forEach(h ->
                    sb.append("\n    ").append(h.getName()).append(": ").append(h.getValue()));
        }

        String body = response.getBody().asPrettyString();
        if (body != null && !body.isBlank()) {
            // Truncate very large bodies to keep logs readable
            sb.append("\n  Body:\n");
            if (body.length() > 2000) {
                sb.append(body, 0, 2000).append("\n    ... [truncated]");
            } else {
                sb.append(body);
            }
        }

        sb.append("\n").append(SEPARATOR);
        log.info(sb.toString());
    }

    private String maskToken(String value) {
        if (value == null || value.length() < 20) return "****";
        return value.substring(0, 10) + "****" + value.substring(value.length() - 4);
    }
}
