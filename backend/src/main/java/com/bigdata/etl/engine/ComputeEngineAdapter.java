package com.bigdata.etl.engine;

import com.bigdata.etl.model.entity.ComputeEngine;
import com.bigdata.etl.model.entity.ComputeJob;

public interface ComputeEngineAdapter {
    boolean supports(String engineType);
    boolean testConnection(ComputeEngine engine);
    String submitJob(ComputeEngine engine, ComputeJob job);
    String getJobStatus(ComputeEngine engine, String engineJobId);
    boolean cancelJob(ComputeEngine engine, String engineJobId);
    QueryResult executeQuery(ComputeEngine engine, String sql);
}
