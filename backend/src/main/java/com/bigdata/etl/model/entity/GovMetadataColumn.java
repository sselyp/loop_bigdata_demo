package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_metadata_column")
public class GovMetadataColumn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tableId;
    private String columnName;
    private Integer ordinalPosition;
    private String dataType;
    private String isNullable;
    private String columnDefault;
    private String columnComment;
    private Integer isPk;
    private Integer isFk;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
