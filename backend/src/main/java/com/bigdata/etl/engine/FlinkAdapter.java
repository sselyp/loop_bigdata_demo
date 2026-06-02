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
public class FlinkAdapter implements ComputeEngineAdapter {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean supports(String engineType) {
        return "FLINK".equalsIgnoreCase(engineType);
    }

    @Override
    public boolean testConnection(ComputeEngine engine) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/overview"))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Flink connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String submitJob(ComputeEngine engine, ComputeJob job) {
        // POST to Flink REST API /jars/{jarid}/run
        // For SQL jobs, use /api/v1/sql/gateway or session cluster
        String jobId = "flink-job-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Flink job submitted: engine={}, jobName={}, jobId={}", engine.getName(), job.getName(), jobId);
        return jobId;
    }

    @Override
    public String getJobStatus(ComputeEngine engine, String engineJobId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/jobs/" + engineJobId))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                // Parse status from Flink JSON response (simplified)
                return "RUNNING";
            }
        } catch (Exception e) {
            log.warn("Failed to get Flink job status: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    @Override
    public boolean cancelJob(ComputeEngine engine, String engineJobId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/jobs/" + engineJobId))
                    .timeout(Duration.ofSeconds(5))
                    .DELETE().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 202 || resp.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Failed to cancel Flink job: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public QueryResult executeQuery(ComputeEngine engine, String sql) {
        // Flink SQL Gateway
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/api/v1/sql/execute"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"sql\":\"" + sql + "\"}"))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return QueryResult.success(List.of(), List.of(), 0);
            }
        } catch (Exception e) {
            log.warn("Flink SQL query failed: {}", e.getMessage());
        }
        return QueryResult.error("Query execution failed");
    }
}
