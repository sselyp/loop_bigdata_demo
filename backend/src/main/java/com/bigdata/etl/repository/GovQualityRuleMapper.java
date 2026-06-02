package com.bigdata.etl.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.etl.model.entity.GovQualityRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GovQualityRuleMapper extends BaseMapper<GovQualityRule> {
}
