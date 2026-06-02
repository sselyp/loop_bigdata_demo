package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("storage_backend")
public class StorageBackend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;       // TIDB, HDFS, S3, MINIO, LOCAL
    private String configJson; // JSON connection config
    private String status;     // ACTIVE, INACTIVE
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
