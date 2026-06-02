package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("storage_quota")
public class StorageQuota {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long backendId;
    private Long maxStorageMb;
    private Integer maxConnections;
    private Integer maxTables;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
