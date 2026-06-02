package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.model.entity.StorageBackend;
import com.bigdata.etl.service.StorageBackendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "存储后端管理")
@RestController
@RequestMapping("/api/storage/backends")
@RequiredArgsConstructor
public class StorageBackendController {

    private final StorageBackendService service;

    @Operation(summary = "存储后端列表")
    @GetMapping
    public Result<List<StorageBackend>> list() {
        return Result.ok(service.listAll());
    }

    @Operation(summary = "获取存储后端")
    @GetMapping("/{id}")
    public Result<StorageBackend> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @Operation(summary = "注册存储后端")
    @PostMapping
    public Result<StorageBackend> create(@RequestBody StorageBackend backend) {
        return Result.ok(service.create(backend));
    }

    @Operation(summary = "更新存储后端")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StorageBackend backend) {
        backend.setId(id);
        service.update(backend);
        return Result.ok();
    }

    @Operation(summary = "删除存储后端")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
