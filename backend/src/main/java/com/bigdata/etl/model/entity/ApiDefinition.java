package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("api_definition")
public class ApiDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String path;
    private String method;
    private String description;
    private String version;
    private Long datasourceId;
    private String queryTemplate;
    private String responseType;
    private Integer rateLimit;
    private Integer cacheTtl;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime deprecatedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
