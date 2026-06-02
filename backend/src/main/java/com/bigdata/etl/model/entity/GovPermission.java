package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_permission")
public class GovPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private Long tableId;
    private Integer grantSelect;
    private Integer grantInsert;
    private Integer grantUpdate;
    private Integer grantDelete;
    private String rowFilterExpr;
    private String columnAllowList;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
