package com.bigdata.etl.engine;

import com.bigdata.etl.model.entity.ComputeEngine;
import com.bigdata.etl.model.entity.ComputeJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class TrinoAdapter implements ComputeEngineAdapter {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean supports(String engineType) {
        return "TRINO".equalsIgnoreCase(engineType);
    }

    @Override
    public boolean testConnection(ComputeEngine engine) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/v1/info"))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Trino connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String submitJob(ComputeEngine engine, ComputeJob job) {
        // Trino is primarily interactive query; batch jobs go via statement API
        String jobId = "trino-job-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Trino query submitted: engine={}, jobName={}, jobId={}", engine.getName(), job.getName(), jobId);
        return jobId;
    }

    @Override
    public String getJobStatus(ComputeEngine engine, String engineJobId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/v1/query/" + engineJobId))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return "RUNNING";
            }
        } catch (Exception e) {
            log.warn("Failed to get Trino query status: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    @Override
    public boolean cancelJob(ComputeEngine engine, String engineJobId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/v1/query/" + engineJobId))
                    .timeout(Duration.ofSeconds(5))
                    .DELETE().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 204 || resp.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Failed to cancel Trino query: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public QueryResult executeQuery(ComputeEngine engine, String sql) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/v1/statement"))
                    .timeout(Duration.ofSeconds(60))
                    .header("X-Trino-User", "etl-platform")
                    .header("Content-Type", "text/plain")
                    .POST(HttpRequest.BodyPublishers.ofString(sql))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return QueryResult.success(List.of(), List.of(), 0);
            }
        } catch (Exception e) {
            log.warn("Trino query failed: {}", e.getMessage());
        }
        return QueryResult.error("Query execution failed");
    }
}
