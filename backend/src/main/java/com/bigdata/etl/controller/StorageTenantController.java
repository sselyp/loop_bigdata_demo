package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.model.entity.StorageQuota;
import com.bigdata.etl.model.entity.StorageTenant;
import com.bigdata.etl.model.entity.StorageUsage;
import com.bigdata.etl.service.StorageTenantService;
import com.bigdata.etl.service.impl.StorageQuotaServiceImpl;
import com.bigdata.etl.service.impl.StorageUsageServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "存储租户管理")
@RestController
@RequestMapping("/api/storage/tenants")
@RequiredArgsConstructor
public class StorageTenantController {

    private final StorageTenantService tenantService;
    private final StorageQuotaServiceImpl quotaService;
    private final StorageUsageServiceImpl usageService;

    @Operation(summary = "租户列表")
    @GetMapping
    public Result<List<StorageTenant>> list() {
        return Result.ok(tenantService.listAll());
    }

    @Operation(summary = "获取租户")
    @GetMapping("/{id}")
    public Result<StorageTenant> get(@PathVariable Long id) {
        return Result.ok(tenantService.getById(id));
    }

    @Operation(summary = "创建租户")
    @PostMapping
    public Result<StorageTenant> create(@RequestBody StorageTenant tenant) {
        return Result.ok(tenantService.create(tenant));
    }

    @Operation(summary = "更新租户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StorageTenant tenant) {
        tenant.setId(id);
        tenantService.update(tenant);
        return Result.ok();
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.ok();
    }

    // -- Quota management nested under tenant --

    @Operation(summary = "获取租户配额")
    @GetMapping("/{id}/quotas")
    public Result<List<StorageQuota>> getQuotas(@PathVariable Long id) {
        return Result.ok(quotaService.listByTenant(id));
    }

    @Operation(summary = "设置租户配额")
    @PostMapping("/{id}/quotas")
    public Result<StorageQuota> createQuota(@PathVariable Long id, @RequestBody StorageQuota quota) {
        quota.setTenantId(id);
        return Result.ok(quotaService.create(quota));
    }

    // -- Usage statistics nested under tenant --

    @Operation(summary = "获取租户用量")
    @GetMapping("/{id}/usage")
    public Result<List<StorageUsage>> getUsage(@PathVariable Long id) {
        return Result.ok(usageService.listByTenant(id));
    }

    @Operation(summary = "重新计算租户用量")
    @PostMapping("/{id}/usage/recalc")
    public Result<Void> recalcUsage(@PathVariable Long id, @RequestParam Long backendId) {
        usageService.recalculate(id, backendId);
        return Result.ok();
    }
}
