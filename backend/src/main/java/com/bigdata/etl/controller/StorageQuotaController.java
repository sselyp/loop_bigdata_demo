package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.model.entity.StorageQuota;
import com.bigdata.etl.service.impl.StorageQuotaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "存储配额管理")
@RestController
@RequestMapping("/api/storage/quotas")
@RequiredArgsConstructor
public class StorageQuotaController {

    private final StorageQuotaServiceImpl quotaService;

    @Operation(summary = "配额列表")
    @GetMapping
    public Result<List<StorageQuota>> list() {
        return Result.ok(quotaService.listAll());
    }

    @Operation(summary = "获取配额")
    @GetMapping("/{id}")
    public Result<StorageQuota> get(@PathVariable Long id) {
        return Result.ok(quotaService.getById(id));
    }

    @Operation(summary = "更新配额")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StorageQuota quota) {
        quota.setId(id);
        quotaService.update(quota);
        return Result.ok();
    }

    @Operation(summary = "删除配额")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        quotaService.delete(id);
        return Result.ok();
    }
}
