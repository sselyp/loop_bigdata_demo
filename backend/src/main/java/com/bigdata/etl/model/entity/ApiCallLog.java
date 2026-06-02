package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("api_call_log")
public class ApiCallLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long apiId;
    private Long apiKeyId;
    private String callerIp;
    private String requestMethod;
    private String requestPath;
    private String requestParams;
    private Integer responseStatus;
    private Long responseTimeMs;
    private String errorMessage;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
