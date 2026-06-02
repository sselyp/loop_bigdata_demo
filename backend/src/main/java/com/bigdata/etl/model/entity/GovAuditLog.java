package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_audit_log")
public class GovAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String operation;
    private String targetType;
    private Long targetId;
    private String operator;
    private String detail;
    private String clientIp;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
