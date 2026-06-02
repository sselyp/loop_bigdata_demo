import request from './index'

export interface Datasource {
  id?: number
  name: string
  type: string
  host: string
  port: number
  database: string
  username: string
  password: string
  status?: string
  remark?: string
}

export const datasourceApi = {
  list: () => request.get('/datasources'),
  create: (data: Datasource) => request.post('/datasources', data),
  update: (id: number, data: Datasource) => request.put(`/datasources/${id}`, data),
  delete: (id: number) => request.delete(`/datasources/${id}`),
  test: (data: Datasource) => request.post('/datasources/test', data)
}
