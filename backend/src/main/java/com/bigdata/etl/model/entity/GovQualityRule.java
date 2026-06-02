package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_quality_rule")
public class GovQualityRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long tableId;
    private String ruleType;
    private String columnName;
    private String ruleConfig;
    private String severity;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
