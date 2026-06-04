# 登录与前端鉴权说明

本文档记录管理端登录页面和前端接口鉴权的实现，方便后续维护。

## 新增功能

- 新增 `/login` 登录页，背景图位于 `frontend/src/assets/login-bg.png`。
- 未登录访问 `/dashboard`、数据源管理、ETL 任务、API 管理、数据治理等页面时，会自动跳转到 `/login`。
- 登录成功后进入原目标页面；直接访问 `/login` 且已登录时，会跳转到 `/dashboard`。
- 主布局右上角新增当前用户展示和退出按钮。

## 默认账号

当前 demo 默认账号为：

```text
admin / admin
```

登录逻辑在 `frontend/src/stores/auth.ts` 中实现。

## 会话策略

登录状态保存在浏览器当前标签页的 `sessionStorage` 中：

- 登录后刷新 dashboard，不需要重新输入密码。
- 关闭标签页或浏览器后再次访问，需要重新登录。
- 点击右上角“退出”会清除当前会话并跳转回 `/login`。

这个策略适合 demo 演示；如果要用于正式环境，建议替换为后端登录接口和 JWT/session。

## API Key 注入

后端当前通过 `ApiKeyInterceptor` 保护 `/api/**` 接口：

- 普通业务接口要求 `X-API-Key`。
- `/api/gateway/keys` 要求 `X-Admin-Key`。

登录成功后，`auth.ts` 会把 demo key 写入当前会话：

```text
apiKey = test-etl-key-12345
adminKey = admin-secret-2024
```

`frontend/src/api/index.ts` 的 Axios 请求拦截器会读取这些值：

- 所有 `/api` 请求自动注入 `X-API-Key`。
- `/gateway/keys` 请求自动注入 `X-Admin-Key`。

## 数据治理页面

当前后端数据治理接口和前端展示契约尚未完全对齐：

- 质量概览、质量趋势、质量维度接口后端暂未提供。
- 血缘图前端需要图结构数据，后端当前提供的是上游/下游关系接口。
- 权限配置页面需要演示字段，后端实体字段不完全一致。

因此 `frontend/src/api/governance.ts` 暂时返回 `frontend/src/mock/governance.ts` 中的 mock 数据，保证演示页面稳定、不弹 404/405 错误。

## 构建与部署

服务器上的前端由 Nginx 容器挂载 `frontend/dist` 提供静态资源。更新前端后执行：

```bash
cd /home/tidb/loop_bigdata_demo/frontend
docker run --rm -v /home/tidb/loop_bigdata_demo/frontend:/app -w /app node:20-alpine sh -lc 'npm run build'
```

构建完成后无需重启 Nginx，`dist` 会被容器直接读取。

如果在本机开发，建议使用 Node 18+ 或 Node 20。Node 16 运行 Vite 5 可能出现 `crypto.getRandomValues is not a function`。

## 相关文件

- `frontend/src/views/Login.vue`
- `frontend/src/stores/auth.ts`
- `frontend/src/router/index.ts`
- `frontend/src/api/index.ts`
- `frontend/src/api/governance.ts`
- `frontend/src/views/Layout.vue`
- `frontend/src/assets/login-bg.png`
