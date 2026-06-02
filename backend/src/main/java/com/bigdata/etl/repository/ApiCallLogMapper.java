package com.bigdata.etl.repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.etl.model.entity.ApiCallLog;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {}
