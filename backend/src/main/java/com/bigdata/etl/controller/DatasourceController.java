package com.bigdata.etl.controller;

import com.bigdata.etl.common.Result;
import com.bigdata.etl.model.entity.Datasource;
import com.bigdata.etl.service.DatasourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据源管理")
@RestController
@RequestMapping("/api/datasources")
@RequiredArgsConstructor
public class DatasourceController {

    private final DatasourceService datasourceService;

    @Operation(summary = "数据源列表")
    @GetMapping
    public Result<List<Datasource>> list() {
        return Result.ok(datasourceService.listAll());
    }

    @Operation(summary = "创建数据源")
    @PostMapping
    public Result<Datasource> create(@Valid @RequestBody Datasource datasource) {
        return Result.ok(datasourceService.create(datasource));
    }

    @Operation(summary = "更新数据源")
    @PutMapping("/{id:\d+}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Datasource datasource) {
        datasource.setId(id);
        datasourceService.update(datasource);
        return Result.ok();
    }

    @Operation(summary = "删除数据源")
    @DeleteMapping("/{id:\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        datasourceService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "测试连接（使用已保存的数据源ID）")
    @PostMapping("/{id:\d+}/test")
    public Result<String> testConnection(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds == null) {
            return Result.fail(404, "数据源不存在");
        }
        boolean ok = datasourceService.testConnection(ds);
        return ok ? Result.ok("连接成功") : Result.fail("连接失败");
    }
}
