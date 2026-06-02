package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.EtlExecution;
import com.bigdata.etl.model.entity.EtlTask;
import java.util.List;

public interface EtlTaskService {
    List<EtlTask> listAll();
    EtlTask create(EtlTask task);
    void update(EtlTask task);
    void delete(Long id);
    Long runTask(Long taskId);
    List<EtlExecution> getExecutions(Long taskId);
}
