package com.bigdata.etl.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.etl.model.entity.StorageBackend;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StorageBackendMapper extends BaseMapper<StorageBackend> {
}
