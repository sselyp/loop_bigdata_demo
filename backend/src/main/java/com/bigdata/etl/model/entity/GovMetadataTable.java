package com.bigdata.etl.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gov_metadata_table")
public class GovMetadataTable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long datasourceId;
    private String tableSchema;
    private String tableName;
    private String tableComment;
    private Long rowCountEstimate;
    private Long storageMb;
    private String owner;
    private String tags;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
