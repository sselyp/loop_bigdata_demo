package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_quality_check")
public class GovQualityCheck {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private String status;
    private String actualValue;
    private String expectedValue;
    private Long errorCount;
    private String executedSql;
    private String checkTime;
    private Long durationMs;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
