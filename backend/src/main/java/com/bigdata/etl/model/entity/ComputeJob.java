package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("compute_job")
public class ComputeJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long engineId;
    private String name;
    private String jobType;    // BATCH/STREAM/QUERY
    private String sqlContent;
    private String jobConfigJson;
    private String status;     // PENDING/RUNNING/SUCCESS/FAILED/CANCELLED
    private LocalDateTime submittedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String resultSummary;
    private String errorMessage;
    private String submittedBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
