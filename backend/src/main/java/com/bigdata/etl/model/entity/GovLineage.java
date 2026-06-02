package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_lineage")
public class GovLineage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sourceTableId;
    private String sourceColumn;
    private Long targetTableId;
    private String targetColumn;
    private String transformExpr;
    private Long etlTaskId;
    private String lineageType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
