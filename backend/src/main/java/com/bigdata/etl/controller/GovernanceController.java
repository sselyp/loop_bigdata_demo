package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.model.entity.*;
import com.bigdata.etl.service.GovernanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据治理")
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class GovernanceController {

    private final GovernanceService governanceService;

    // -- Metadata --

    @Operation(summary = "元数据-表列表")
    @GetMapping("/metadata/tables")
    public Result<List<GovMetadataTable>> listTables(@RequestParam(required = false) Long datasourceId) {
        return Result.ok(governanceService.listTables(datasourceId));
    }

    @Operation(summary = "元数据-表详情")
    @GetMapping("/metadata/tables/{id}")
    public Result<GovMetadataTable> getTable(@PathVariable Long id) {
        return Result.ok(governanceService.getTable(id));
    }

    @Operation(summary = "元数据-列列表")
    @GetMapping("/metadata/tables/{tableId}/columns")
    public Result<List<GovMetadataColumn>> listColumns(@PathVariable Long tableId) {
        return Result.ok(governanceService.listColumns(tableId));
    }

    @Operation(summary = "同步元数据")
    @PostMapping("/metadata/sync/{datasourceId}")
    public Result<Void> syncMetadata(@PathVariable Long datasourceId) {
        governanceService.syncMetadata(datasourceId);
        return Result.ok();
    }

    // -- Lineage --

    @Operation(summary = "上游血缘")
    @GetMapping("/lineage/upstream/{tableId}")
    public Result<List<GovLineage>> getUpstream(@PathVariable Long tableId) {
        return Result.ok(governanceService.getUpstreamLineage(tableId));
    }

    @Operation(summary = "下游血缘")
    @GetMapping("/lineage/downstream/{tableId}")
    public Result<List<GovLineage>> getDownstream(@PathVariable Long tableId) {
        return Result.ok(governanceService.getDownstreamLineage(tableId));
    }

    @Operation(summary = "添加血缘关系")
    @PostMapping("/lineage")
    public Result<GovLineage> addLineage(@RequestBody GovLineage lineage) {
        return Result.ok(governanceService.addLineage(lineage));
    }

    // -- Quality --

    @Operation(summary = "质量规则列表")
    @GetMapping("/quality/rules")
    public Result<List<GovQualityRule>> listQualityRules(@RequestParam(required = false) Long tableId) {
        return Result.ok(governanceService.listQualityRules(tableId));
    }

    @Operation(summary = "创建质量规则")
    @PostMapping("/quality/rules")
    public Result<GovQualityRule> createQualityRule(@RequestBody GovQualityRule rule) {
        return Result.ok(governanceService.createQualityRule(rule));
    }

    @Operation(summary = "更新质量规则")
    @PutMapping("/quality/rules/{id}")
    public Result<Void> updateQualityRule(@PathVariable Long id, @RequestBody GovQualityRule rule) {
        rule.setId(id);
        governanceService.updateQualityRule(rule);
        return Result.ok();
    }

    @Operation(summary = "删除质量规则")
    @DeleteMapping("/quality/rules/{id}")
    public Result<Void> deleteQualityRule(@PathVariable Long id) {
        governanceService.deleteQualityRule(id);
        return Result.ok();
    }

    @Operation(summary = "执行质量检查")
    @PostMapping("/quality/rules/{id}/check")
    public Result<GovQualityCheck> runQualityCheck(@PathVariable Long id) {
        return Result.ok(governanceService.runQualityCheck(id));
    }

    @Operation(summary = "质量检查记录")
    @GetMapping("/quality/rules/{ruleId}/checks")
    public Result<List<GovQualityCheck>> listQualityChecks(@PathVariable Long ruleId) {
        return Result.ok(governanceService.listQualityChecks(ruleId));
    }

    // -- Masking --

    @Operation(summary = "脱敏规则列表")
    @GetMapping("/masking/rules")
    public Result<List<GovMaskingRule>> listMaskingRules(@RequestParam(required = false) Long tableId) {
        return Result.ok(governanceService.listMaskingRules(tableId));
    }

    @Operation(summary = "创建脱敏规则")
    @PostMapping("/masking/rules")
    public Result<GovMaskingRule> createMaskingRule(@RequestBody GovMaskingRule rule) {
        return Result.ok(governanceService.createMaskingRule(rule));
    }

    @Operation(summary = "更新脱敏规则")
    @PutMapping("/masking/rules/{id}")
    public Result<Void> updateMaskingRule(@PathVariable Long id, @RequestBody GovMaskingRule rule) {
        rule.setId(id);
        governanceService.updateMaskingRule(rule);
        return Result.ok();
    }

    @Operation(summary = "删除脱敏规则")
    @DeleteMapping("/masking/rules/{id}")
    public Result<Void> deleteMaskingRule(@PathVariable Long id) {
        governanceService.deleteMaskingRule(id);
        return Result.ok();
    }

    @Operation(summary = "测试脱敏效果")
    @PostMapping("/masking/rules/{id}/apply")
    public Result<String> applyMasking(@PathVariable Long id, @RequestBody String value) {
        return Result.ok(governanceService.applyMasking(id, value));
    }

    // -- Permission --

    @Operation(summary = "权限列表")
    @GetMapping("/permissions")
    public Result<List<GovPermission>> listPermissions(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Long tableId) {
        return Result.ok(governanceService.listPermissions(roleName, tableId));
    }

    @Operation(summary = "授予权限")
    @PostMapping("/permissions")
    public Result<GovPermission> grantPermission(@RequestBody GovPermission permission) {
        return Result.ok(governanceService.grantPermission(permission));
    }

    @Operation(summary = "撤销权限")
    @DeleteMapping("/permissions/{id}")
    public Result<Void> revokePermission(@PathVariable Long id) {
        governanceService.revokePermission(id);
        return Result.ok();
    }

    @Operation(summary = "检查权限")
    @GetMapping("/permissions/check")
    public Result<Boolean> checkPermission(
            @RequestParam String roleName,
            @RequestParam Long tableId,
            @RequestParam String operation) {
        return Result.ok(governanceService.checkPermission(roleName, tableId, operation));
    }

    // -- Audit --

    @Operation(summary = "审计日志")
    @GetMapping("/audit")
    public Result<List<GovAuditLog>> listAuditLogs(
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String operation,
            @RequestParam(defaultValue = "100") int limit) {
        return Result.ok(governanceService.listAuditLogs(operator, operation, limit));
    }
}
