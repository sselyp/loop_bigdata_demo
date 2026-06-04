package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.model.entity.*;
import com.bigdata.etl.service.ApiGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "API网关管理")
@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class ApiGatewayController {

    private final ApiGatewayService gatewayService;

    // -- API Definitions --

    @Operation(summary = "API列表")
    @GetMapping("/apis")
    public Result<List<ApiDefinition>> listApis(@RequestParam(required = false) String status) {
        return Result.ok(gatewayService.listApis(status));
    }

    @Operation(summary = "获取API")
    @GetMapping("/apis/{id}")
    public Result<ApiDefinition> getApi(@PathVariable Long id) {
        return Result.ok(gatewayService.getApi(id));
    }

    @Operation(summary = "创建API")
    @PostMapping("/apis")
    public Result<ApiDefinition> createApi(@RequestBody ApiDefinition api) {
        return Result.ok(gatewayService.createApi(api));
    }

    @Operation(summary = "更新API")
    @PutMapping("/apis/{id}")
    public Result<Void> updateApi(@PathVariable Long id, @RequestBody ApiDefinition api) {
        api.setId(id);
        gatewayService.updateApi(api);
        return Result.ok();
    }

    @Operation(summary = "删除API")
    @DeleteMapping("/apis/{id}")
    public Result<Void> deleteApi(@PathVariable Long id) {
        gatewayService.deleteApi(id);
        return Result.ok();
    }

    @Operation(summary = "发布API")
    @PostMapping("/apis/{id}/publish")
    public Result<Void> publishApi(@PathVariable Long id) {
        gatewayService.publishApi(id);
        return Result.ok();
    }

    @Operation(summary = "废弃API")
    @PostMapping("/apis/{id}/deprecate")
    public Result<Void> deprecateApi(@PathVariable Long id) {
        gatewayService.deprecateApi(id);
        return Result.ok();
    }

    // -- API Keys --

    @Operation(summary = "密钥列表")
    @GetMapping("/keys")
    public Result<List<ApiKey>> listKeys() {
        return Result.ok(gatewayService.listKeys());
    }

    @Operation(summary = "创建密钥")
    @PostMapping("/keys")
    public Result<ApiKey> createKey(@RequestBody ApiKey key) {
        return Result.ok(gatewayService.createKey(key));
    }

    @Operation(summary = "吊销密钥")
    @PostMapping("/keys/{id}/revoke")
    public Result<Void> revokeKey(@PathVariable Long id) {
        gatewayService.revokeKey(id);
        return Result.ok();
    }

    @Operation(summary = "验证密钥")
    @PostMapping("/keys/validate")
    public Result<Boolean> validateKey(@RequestBody java.util.Map<String,String> body) {
        return Result.ok(gatewayService.validateKey(body.getOrDefault("keyValue", body.getOrDefault("key", ""))));
    }

    // -- Call Logs --

    @Operation(summary = "API调用日志")
    @GetMapping("/logs")
    public Result<List<ApiCallLog>> listCallLogs(
            @RequestParam(required = false) Long apiId,
            @RequestParam(defaultValue = "100") int limit) {
        return Result.ok(gatewayService.listCallLogs(apiId, limit));
    }
}
