# ETL核心链路测试计划 (P1) — T13

**版本**: v1.0
**日期**: 2026-06-02
**负责人**: claudcode-测试
**关联任务**: #t2 (后端ETL), #t7/#t8 (前端ETL)

---

## 1. 测试范围

| 模块 | API前缀 | 覆盖优先级 |
|------|---------|-----------|
| 数据源管理 | `/api/datasources` | P1 |
| ETL任务管理 | `/api/etl/tasks` | P1 |
| 任务执行与监控 | `/api/etl/tasks/{id}/run`, `/logs` | P1 |

---

## 2. 风险识别

| 风险 | 严重度 | 依据 |
|------|--------|------|
| 密码明文存储 (`Datasource.password` 字段无加密标识) | 高 | 实体类直接存 password 字段，未见加密注解 |
| 列表接口无分页 (`listAll()` 全量查询) | 中 | DatasourceService/EtlTaskService 返回 `List<>` |
| 增量同步字段不存在时无校验 (`incrementalColumn`) | 高 | EtlTask 模型中 incrementalColumn 为普通字符串，运行时才会失败 |
| `fieldMapping` JSON 解析失败无兜底 | 中 | 存为 String，反序列化异常可能导致任务静默失败 |
| 任务执行为异步，前端轮询机制未确认 | 中 | `runTask()` 返回 executionId，状态更新依赖异步线程 |
| 接口无认证鉴权 (未见 Spring Security 配置) | 高 | Controller 层无 @PreAuthorize，骨架阶段待确认 |
| 软删除标志 (`deleted`) 未在查询中过滤 | 中 | 需确认 MyBatis-Plus 全局逻辑删除配置 |

---

## 3. 测试用例

### 3.1 数据源管理

#### TC-DS-001: 创建有效数据源
- **前提**: 服务启动，demo_db 已存在
- **输入**: `POST /api/datasources` body = `{name:"test-mysql", type:"MYSQL", host:"localhost", port:3306, database:"demo_db", username:"root", password:"xxx"}`
- **预期**: HTTP 200, 返回含自增 id 的数据源对象，status="ACTIVE"
- **验证点**: TiDB `etl_datasource` 表有对应记录，deleted=0

#### TC-DS-002: 测试连接 — 正常路径
- **输入**: `POST /api/datasources/test` body = 已知有效 MySQL 连接参数
- **预期**: `{"code":200, "data":"连接成功"}`

#### TC-DS-003: 测试连接 — 异常路径 (错误密码)
- **输入**: `POST /api/datasources/test` body = 密码错误的参数
- **预期**: `{"code":xxx, "data":"连接失败"}` 且不抛出 500

#### TC-DS-004: 删除数据源 (逻辑删除)
- **输入**: `DELETE /api/datasources/{id}`
- **预期**: HTTP 200; `etl_datasource` 表该记录 `deleted=1`; 再次 GET list 不返回该记录

#### TC-DS-005: 更新数据源
- **输入**: `PUT /api/datasources/{id}` 修改 host/port
- **预期**: HTTP 200; 数据库记录更新; updateTime 刷新

#### TC-DS-006: 列表查询含已删除记录 (边界)
- **步骤**: 先删除一条，再 GET list
- **预期**: 已删除记录不出现 (验证 MyBatis-Plus 逻辑删除生效)

---

### 3.2 ETL 任务管理

#### TC-TASK-001: 创建全量同步任务
- **输入**: `POST /api/etl/tasks` body = `{name:"full-sync", sourceDatasourceId:1, sourceTable:"orders", targetTable:"etl_orders", syncMode:"FULL", scheduleType:"MANUAL"}`
- **预期**: HTTP 200, 返回任务对象; `etl_task` 表有记录; status="ENABLED"

#### TC-TASK-002: 创建增量同步任务
- **输入**: syncMode="INCREMENTAL", incrementalColumn="updated_at"
- **预期**: 任务正常创建; incrementalColumn 存储正确

#### TC-TASK-003: 创建增量任务但 incrementalColumn 为空 (边界)
- **输入**: syncMode="INCREMENTAL", incrementalColumn=null
- **预期**: 应返回 400 或业务错误码，而非创建成功后运行时崩溃

#### TC-TASK-004: fieldMapping 格式非法 (边界)
- **输入**: fieldMapping="not-a-json"
- **预期**: 应返回 400；不应创建成功后执行时静默失败

#### TC-TASK-005: 删除不存在任务
- **输入**: `DELETE /api/etl/tasks/99999`
- **预期**: 返回业务错误码，不返回 500

---

### 3.3 任务执行与监控

#### TC-RUN-001: 手动执行任务 — 正常
- **前提**: 数据源 demo_db 可达，目标表已建
- **步骤**: `POST /api/etl/tasks/{id}/run`
- **预期**: HTTP 200 返回 executionId; `etl_execution` 表新增记录

#### TC-RUN-002: 执行状态最终为 SUCCESS
- **步骤**: 触发执行 -> 轮询 `GET /api/etl/tasks/{id}/logs` 直到 lastRunStatus != RUNNING
- **预期**: lastRunStatus="SUCCESS"; 目标表数据条数 >= 源表条数

#### TC-RUN-003: 执行数据一致性校验
- **步骤**: 全量同步 demo_db.orders (16条) 到 TiDB 目标表
- **预期**: 目标表行数 = 16; 关键字段 (id, amount, status) 值一致

#### TC-RUN-004: 重复执行全量任务
- **步骤**: 对同一任务执行两次
- **预期**: 第二次执行后目标表无重复数据 (幂等); `etl_execution` 新增第二条记录

#### TC-RUN-005: 执行日志查看
- **输入**: `GET /api/etl/tasks/{id}/logs`
- **预期**: 返回执行记录列表，包含 startTime、endTime、status、rowCount 等关键字段

#### TC-RUN-006: 执行不存在任务
- **输入**: `POST /api/etl/tasks/99999/run`
- **预期**: 返回业务错误码，非 500

---

## 4. 非功能检查项

| 检查项 | 方法 | 通过标准 |
|--------|------|---------|
| 密码字段加密 | 查看 DB 存储值 | 非明文存储 |
| 列表接口性能 (1000条) | 造 1000 条数据后 GET list | 响应 < 1s |
| 并发执行同一任务 | 同时发 2 次 POST run | 第二次拒绝或排队，不重复写入 |
| 接口认证 | 不带 token 访问 | 返回 401 (如已实现认证) |

---

## 5. 测试数据依赖

| 数据 | 来源 | 备注 |
|------|------|------|
| demo_db.orders / users / products | 服务器已建 (16条) | CEO已准备 |
| TiDB etl_platform 目标表 | 服务器已建 | CEO已准备 |
| 无效数据源配置 | 测试时构造 | 错误密码/不存在主机 |

---

## 6. 阻塞项 & 前置条件

- [ ] 后端服务需成功部署并可访问 (等待 @deepseek / @codex 完成 #t2)
- [ ] 确认 Spring Security 是否已集成认证 (影响 TC 请求 Header)
- [ ] 确认任务执行引擎实现方式 (同步/异步线程池/SeaTunnel) — 影响轮询策略
- [ ] 确认 MyBatis-Plus 全局逻辑删除配置已生效

---

## 7. 发布门控标准 (Go/No-Go)

**必须全部通过后方可合并 P1 功能**:
- TC-DS-001/002/003 全部通过
- TC-TASK-001/002 通过
- TC-RUN-001/002/003 通过 (数据一致性)
- 无 P0 级别 Bug 开放

**允许带入下轮修复**:
- TC-DS-005/006 边界用例
- 非功能性检查项 (性能/并发)
