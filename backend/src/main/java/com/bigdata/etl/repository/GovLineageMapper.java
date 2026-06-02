package com.bigdata.etl.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.etl.model.entity.GovLineage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GovLineageMapper extends BaseMapper<GovLineage> {
}
