package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("etl_execution")
public class EtlExecution {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String status;      // RUNNING, SUCCESS, FAILED
    private Long rowsProcessed;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
