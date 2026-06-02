package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("storage_usage")
public class StorageUsage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long backendId;
    private Long usedStorageMb;
    private Integer tableCount;
    private LocalDateTime lastCalcTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
