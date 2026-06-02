package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("compute_engine")
public class ComputeEngine {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;       // FLINK/SPARK/TRINO
    private String endpoint;
    private String configJson;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
