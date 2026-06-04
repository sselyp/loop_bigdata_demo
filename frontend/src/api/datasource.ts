import request, { type ApiResponse } from './index'

export interface Datasource {
  id?: number
  name: string
  type: string // MYSQL / POSTGRESQL / ORACLE / SQLSERVER
  host: string
  port: number
  database: string
  username: string
  password?: string // 后端 WRITE_ONLY，列表不返回
  status?: string // ACTIVE / INACTIVE
  remark?: string
}

export const datasourceApi = {
  list: () => request.get<unknown, ApiResponse<Datasource[]>>('/datasources'),
  create: (data: Datasource) => request.post<unknown, ApiResponse<Datasource>>('/datasources', data),
  update: (id: number, data: Datasource) =>
    request.put<unknown, ApiResponse<void>>(`/datasources/${id}`, data),
  delete: (id: number) => request.delete<unknown, ApiResponse<void>>(`/datasources/${id}`),
  // 后端仅支持按已保存数据源 ID 测试连接：POST /datasources/{id}/test
  test: (id: number) => request.post<unknown, ApiResponse<string>>(`/datasources/${id}/test`)
}
