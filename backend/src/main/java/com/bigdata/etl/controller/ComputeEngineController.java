package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.engine.QueryResult;
import com.bigdata.etl.model.entity.ComputeEngine;
import com.bigdata.etl.model.entity.ComputeJob;
import com.bigdata.etl.service.ComputeEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "计算引擎管理")
@RestController
@RequestMapping("/api/compute")
@RequiredArgsConstructor
public class ComputeEngineController {

    private final ComputeEngineService engineService;

    // -- Engine endpoints --

    @Operation(summary = "引擎列表")
    @GetMapping("/engines")
    public Result<List<ComputeEngine>> listEngines() {
        return Result.ok(engineService.listEngines());
    }

    @Operation(summary = "注册引擎")
    @PostMapping("/engines")
    public Result<ComputeEngine> register(@RequestBody ComputeEngine engine) {
        return Result.ok(engineService.registerEngine(engine));
    }

    @Operation(summary = "获取引擎")
    @GetMapping("/engines/{id}")
    public Result<ComputeEngine> getEngine(@PathVariable Long id) {
        return Result.ok(engineService.getEngine(id));
    }

    @Operation(summary = "更新引擎")
    @PutMapping("/engines/{id}")
    public Result<Void> updateEngine(@PathVariable Long id, @RequestBody ComputeEngine engine) {
        engine.setId(id);
        engineService.updateEngine(engine);
        return Result.ok();
    }

    @Operation(summary = "删除引擎")
    @DeleteMapping("/engines/{id}")
    public Result<Void> deleteEngine(@PathVariable Long id) {
        engineService.deleteEngine(id);
        return Result.ok();
    }

    @Operation(summary = "测试引擎连接")
    @PostMapping("/engines/{id}/test")
    public Result<String> testConnection(@PathVariable Long id) {
        boolean ok = engineService.testConnection(id);
        return ok ? Result.ok("连接成功") : Result.fail("连接失败");
    }

    // -- Job endpoints --

    @Operation(summary = "任务列表")
    @GetMapping("/jobs")
    public Result<List<ComputeJob>> listJobs(@RequestParam(required = false) Long engineId) {
        return Result.ok(engineService.listJobs(engineId));
    }

    @Operation(summary = "提交任务")
    @PostMapping("/engines/{engineId}/jobs")
    public Result<ComputeJob> submitJob(@PathVariable Long engineId, @RequestBody ComputeJob job) {
        return Result.ok(engineService.submitJob(engineId, job));
    }

    @Operation(summary = "获取任务状态")
    @GetMapping("/jobs/{id}/status")
    public Result<String> getJobStatus(@PathVariable Long id) {
        return Result.ok(engineService.getJobStatus(id));
    }

    @Operation(summary = "取消任务")
    @PostMapping("/jobs/{id}/cancel")
    public Result<String> cancelJob(@PathVariable Long id) {
        boolean ok = engineService.cancelJob(id);
        return ok ? Result.ok("取消成功") : Result.fail("取消失败");
    }

    // -- Interactive query --

    @Operation(summary = "交互式SQL查询")
    @PostMapping("/engines/{id}/query")
    public Result<QueryResult> executeQuery(@PathVariable Long id, @RequestBody String sql) {
        return Result.ok(engineService.executeQuery(id, sql));
    }
}
