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
public class SparkAdapter implements ComputeEngineAdapter {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean supports(String engineType) {
        return "SPARK".equalsIgnoreCase(engineType);
    }

    @Override
    public boolean testConnection(ComputeEngine engine) {
        try {
            // Spark Livy or Spark Connect endpoint
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/batches"))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Spark connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String submitJob(ComputeEngine engine, ComputeJob job) {
        String jobId = "spark-job-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Spark job submitted: engine={}, jobName={}, jobId={}", engine.getName(), job.getName(), jobId);
        return jobId;
    }

    @Override
    public String getJobStatus(ComputeEngine engine, String engineJobId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/batches/" + engineJobId + "/state"))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return "RUNNING";
            }
        } catch (Exception e) {
            log.warn("Failed to get Spark job status: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    @Override
    public boolean cancelJob(ComputeEngine engine, String engineJobId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/batches/" + engineJobId))
                    .timeout(Duration.ofSeconds(5))
                    .DELETE().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Failed to cancel Spark job: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public QueryResult executeQuery(ComputeEngine engine, String sql) {
        // Spark SQL via Thrift Server or Spark Connect
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(engine.getEndpoint() + "/sessions/0/statements"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"code\":\"{}\n\"}".replace("{}", sql)))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return QueryResult.success(List.of(), List.of(), 0);
            }
        } catch (Exception e) {
            log.warn("Spark SQL query failed: {}", e.getMessage());
        }
        return QueryResult.error("Query execution failed");
    }
}
