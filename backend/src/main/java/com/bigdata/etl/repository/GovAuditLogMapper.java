package com.bigdata.etl.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.etl.model.entity.GovAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GovAuditLogMapper extends BaseMapper<GovAuditLog> {
}
