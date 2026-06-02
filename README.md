# loop_bigdata_demo

大数据平台 ETL 接入模块 Demo

## 技术栈

| 层 | 技术 |
|---|---|
| 数据库 | TiDB (MySQL compatible) |
| 后端 | Spring Boot 3 + MyBatis-Plus |
| 前端 | Vue 3 + TypeScript + Ant Design Vue |
| 部署 | Docker Compose |

## 项目结构

```
loop_bigdata_demo/
├── backend/          # Spring Boot ETL服务
├── frontend/         # Vue 3 管理界面
├── docs/             # 接口文档与设计文档
└── docker-compose.yml
```

## 核心功能

1. 数据源管理（MySQL/PG/Oracle/SQL Server → TiDB）
2. ETL任务配置（全量/增量同步，字段映射）
3. 任务调度执行（手动/Cron）
4. 运行监控与日志查看

## API 文档

启动后访问：`http://localhost:8080/swagger-ui.html`
