package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_masking_rule")
public class GovMaskingRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long tableId;
    private String columnName;
    private String maskType;
    private String maskConfig;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
