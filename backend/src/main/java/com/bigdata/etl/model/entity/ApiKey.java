package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("api_key")
public class ApiKey {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String keyValue;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String secretValue;
    private String roleName;
    private Integer rateLimitOverride;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
