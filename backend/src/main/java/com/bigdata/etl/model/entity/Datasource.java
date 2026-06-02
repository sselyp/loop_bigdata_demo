package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("etl_datasource")
public class Datasource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    @TableField("`type`")
    private String type;       // MYSQL, POSTGRESQL, ORACLE, SQLSERVER
    private String host;
    private Integer port;
    @TableField("`database`")
    private String database;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String status;     // ACTIVE, INACTIVE
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
