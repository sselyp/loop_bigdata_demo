package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.ComputeEngine;
import com.bigdata.etl.model.entity.ComputeJob;
import com.bigdata.etl.engine.QueryResult;
import java.util.List;

public interface ComputeEngineService {
    List<ComputeEngine> listEngines();
    ComputeEngine getEngine(Long id);
    ComputeEngine registerEngine(ComputeEngine engine);
    void updateEngine(ComputeEngine engine);
    void deleteEngine(Long id);
    boolean testConnection(Long engineId);

    List<ComputeJob> listJobs(Long engineId);
    ComputeJob submitJob(Long engineId, ComputeJob job);
    String getJobStatus(Long jobId);
    boolean cancelJob(Long jobId);

    QueryResult executeQuery(Long engineId, String sql);
}
