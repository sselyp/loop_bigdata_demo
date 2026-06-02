import request from './index'

export interface EtlTask {
  id?: number
  name: string
  sourceDatasourceId: number
  sourceTable: string
  targetTable: string
  syncMode: string
  incrementalColumn?: string
  fieldMapping?: string
  scheduleType: string
  cronExpression?: string
  status?: string
  remark?: string
}

export const etlTaskApi = {
  list: () => request.get('/etl/tasks'),
  create: (data: EtlTask) => request.post('/etl/tasks', data),
  update: (id: number, data: EtlTask) => request.put(`/etl/tasks/${id}`, data),
  delete: (id: number) => request.delete(`/etl/tasks/${id}`),
  run: (id: number) => request.post(`/etl/tasks/${id}/run`),
  logs: (id: number) => request.get(`/etl/tasks/${id}/logs`)
}
export interface EtlExecution {
  id?: number
  taskId: number
  status: string
  rowsProcessed: number
  errorMessage?: string
  startTime?: string
  endTime?: string
  createTime?: string
}

// Extend etlTaskApi with executions method
etlTaskApi.executions = (id: number) => request.get(`/etl/tasks/${id}/logs`)
etlTaskApi.listAll = () => request.get('/etl/tasks')
