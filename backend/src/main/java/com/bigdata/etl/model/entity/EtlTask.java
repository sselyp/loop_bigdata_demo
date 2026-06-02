package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("etl_task")
public class EtlTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long sourceDatasourceId;
    private String sourceTable;
    private String targetTable;
    private String syncMode;       // FULL, INCREMENTAL
    private String incrementalColumn;  // for INCREMENTAL mode
    private String fieldMapping;   // JSON string: [{source, target}]
    private String scheduleType;   // MANUAL, CRON
    private String cronExpression;
    private String status;         // ENABLED, DISABLED
    private String lastRunStatus;  // SUCCESS, FAILED, RUNNING
    private LocalDateTime lastRunTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
