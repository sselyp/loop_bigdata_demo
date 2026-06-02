package com.bigdata.etl.service.impl;

import com.bigdata.etl.engine.ComputeEngineAdapter;
import com.bigdata.etl.engine.EngineAdapterFactory;
import com.bigdata.etl.engine.QueryResult;
import com.bigdata.etl.model.entity.ComputeEngine;
import com.bigdata.etl.model.entity.ComputeJob;
import com.bigdata.etl.repository.ComputeEngineMapper;
import com.bigdata.etl.repository.ComputeJobMapper;
import com.bigdata.etl.service.ComputeEngineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComputeEngineServiceImpl implements ComputeEngineService {

    private final ComputeEngineMapper engineMapper;
    private final ComputeJobMapper jobMapper;
    private final EngineAdapterFactory adapterFactory;

    // -- Engine management --

    @Override
    public List<ComputeEngine> listEngines() {
        LambdaQueryWrapper<ComputeEngine> w = new LambdaQueryWrapper<>();
        w.orderByDesc(ComputeEngine::getCreateTime);
        return engineMapper.selectList(w);
    }

    @Override
    public ComputeEngine getEngine(Long id) {
        return engineMapper.selectById(id);
    }

    @Override
    @Transactional
    public ComputeEngine registerEngine(ComputeEngine engine) {
        if (engine.getStatus() == null || engine.getStatus().isBlank()) {
            engine.setStatus("ACTIVE");
        }
        engineMapper.insert(engine);
        log.info("Registered compute engine: id={}, name={}, type={}", engine.getId(), engine.getName(), engine.getType());
        return engine;
    }

    @Override
    public void updateEngine(ComputeEngine engine) {
        engineMapper.updateById(engine);
        log.info("Updated compute engine: id={}", engine.getId());
    }

    @Override
    public void deleteEngine(Long id) {
        engineMapper.deleteById(id);
        log.info("Deleted compute engine: id={}", id);
    }

    @Override
    public boolean testConnection(Long engineId) {
        ComputeEngine engine = engineMapper.selectById(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Engine not found: " + engineId);
        }
        ComputeEngineAdapter adapter = adapterFactory.getAdapter(engine.getType());
        return adapter.testConnection(engine);
    }

    // -- Job management --

    @Override
    public List<ComputeJob> listJobs(Long engineId) {
        LambdaQueryWrapper<ComputeJob> w = new LambdaQueryWrapper<>();
        if (engineId != null) {
            w.eq(ComputeJob::getEngineId, engineId);
        }
        w.orderByDesc(ComputeJob::getCreateTime);
        return jobMapper.selectList(w);
    }

    @Override
    @Transactional
    public ComputeJob submitJob(Long engineId, ComputeJob job) {
        ComputeEngine engine = engineMapper.selectById(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Engine not found: " + engineId);
        }
        job.setEngineId(engineId);
        if (job.getStatus() == null || job.getStatus().isBlank()) {
            job.setStatus("PENDING");
        }

        ComputeEngineAdapter adapter = adapterFactory.getAdapter(engine.getType());

        try {
            job.setSubmittedAt(LocalDateTime.now());
            job.setStatus("RUNNING");
            jobMapper.insert(job);

            String engineJobId = adapter.submitJob(engine, job);
            job.setJobConfigJson(job.getJobConfigJson()); // engine job ID tracked in config
            job.setStartedAt(LocalDateTime.now());
            jobMapper.updateById(job);

            log.info("Job submitted: id={}, name={}, engineJobId={}", job.getId(), job.getName(), engineJobId);
        } catch (Exception e) {
            log.error("Failed to submit job: {}", e.getMessage());
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            if (job.getId() != null) {
                jobMapper.updateById(job);
            } else {
                jobMapper.insert(job);
            }
        }
        return job;
    }

    @Override
    public String getJobStatus(Long jobId) {
        ComputeJob job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }
        ComputeEngine engine = engineMapper.selectById(job.getEngineId());
        ComputeEngineAdapter adapter = adapterFactory.getAdapter(engine.getType());
        String status = adapter.getJobStatus(engine, job.getJobConfigJson());
        return status;
    }

    @Override
    @Transactional
    public boolean cancelJob(Long jobId) {
        ComputeJob job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }
        ComputeEngine engine = engineMapper.selectById(job.getEngineId());
        ComputeEngineAdapter adapter = adapterFactory.getAdapter(engine.getType());
        boolean cancelled = adapter.cancelJob(engine, job.getJobConfigJson());
        if (cancelled) {
            job.setStatus("CANCELLED");
            job.setFinishedAt(LocalDateTime.now());
            jobMapper.updateById(job);
        }
        return cancelled;
    }

    // -- Interactive query --

    @Override
    public QueryResult executeQuery(Long engineId, String sql) {
        ComputeEngine engine = engineMapper.selectById(engineId);
        if (engine == null) {
            return QueryResult.error("Engine not found: " + engineId);
        }
        ComputeEngineAdapter adapter = adapterFactory.getAdapter(engine.getType());
        long start = System.currentTimeMillis();
        QueryResult result = adapter.executeQuery(engine, sql);
        if (result.getElapsedMs() == 0) {
            result.setElapsedMs(System.currentTimeMillis() - start);
        }
        return result;
    }
}
