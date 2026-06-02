package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("etl_datasource")
public class Datasource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;       // MYSQL, POSTGRESQL, ORACLE, SQLSERVER
    private String host;
    private Integer port;
    private String database;
    private String username;
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
