package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.model.entity.EtlExecution;
import com.bigdata.etl.model.entity.EtlTask;
import com.bigdata.etl.service.EtlTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ETL任务管理")
@RestController
@RequestMapping("/api/etl/tasks")
@RequiredArgsConstructor
public class EtlTaskController {

    private final EtlTaskService etlTaskService;

    @Operation(summary = "任务列表")
    @GetMapping
    public Result<List<EtlTask>> list() {
        return Result.ok(etlTaskService.listAll());
    }

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<EtlTask> create(@Valid @RequestBody EtlTask task) {
        return Result.ok(etlTaskService.create(task));
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody EtlTask task) {
        task.setId(id);
        etlTaskService.update(task);
        return Result.ok();
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        etlTaskService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "手动执行任务")
    @PostMapping("/{id}/run")
    public Result<Long> run(@PathVariable Long id) {
        Long executionId = etlTaskService.runTask(id);
        return Result.ok(executionId);
    }

    @Operation(summary = "查看执行日志")
    @GetMapping("/{id}/logs")
    public Result<List<EtlExecution>> logs(@PathVariable Long id) {
        return Result.ok(etlTaskService.getExecutions(id));
    }
}
