# SkyEye 智能监控系统

SkyEye 是一个基于人工智能技术的综合性安全监控平台，集成了视频监控、设备管理、智能分析、报警处理、数据分析等功能模块。

## 系统架构

### 技术栈

**前端**
- Vue.js 3 + TypeScript
- Element Plus UI 组件库
- Vite 构建工具
- Pinia 状态管理
- Vue Router 路由管理

**后端**
- Spring Boot 2.7.x
- Spring Cloud Gateway
- Spring Security + JWT
- PostgreSQL 数据库
- Redis 缓存
- RabbitMQ 消息队列

**基础设施**
- Docker & Docker Compose
- MinIO 对象存储
- Elasticsearch + Kibana 日志分析
- Prometheus + Grafana 监控

## 快速开始

### 环境要求

- Java 11+
- Node.js 16+
- Docker & Docker Compose
- Maven 3.6+

### 1. 克隆项目

```bash
git clone <repository-url>
cd skyeye-monitoring-system
```

### 2. 启动基础设施服务

```bash
# Windows
scripts\setup-dev-env.bat

# Linux/Mac
docker-compose up -d postgres redis rabbitmq minio elasticsearch
```

### 3. 启动后端服务

```bash
cd skyeye-backend

# 编译项目
mvn clean install

# 启动网关服务
mvn spring-boot:run -pl skyeye-gateway

# 启动认证服务（新终端）
mvn spring-boot:run -pl skyeye-auth
```

### 4. 启动前端服务

```bash
cd skyeye-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 5. 访问系统

- 前端界面: http://localhost:3000
- API 网关: http://localhost:8080
- 认证服务: http://localhost:8081

## 服务端口

| 服务 | 端口 | 描述 |
|------|------|------|
| 前端 | 3000 | Vue.js 开发服务器 |
| API网关 | 8080 | Spring Cloud Gateway |
| 认证服务 | 8081 | 用户认证和权限管理 |
| PostgreSQL | 5432 | 主数据库 |
| Redis | 6379 | 缓存服务 |
| RabbitMQ | 5672/15672 | 消息队列/管理界面 |
| MinIO | 9000/9001 | 对象存储/控制台 |
| Elasticsearch | 9200 | 搜索引擎 |
| Kibana | 5601 | 日志分析界面 |
| Prometheus | 9090 | 监控数据收集 |
| Grafana | 3001 | 监控仪表板 |

## 默认账户

### 系统管理员
- 用户名: `admin`
- 密码: `admin123`

### 基础设施服务
- RabbitMQ: `skyeye` / `skyeye123`
- MinIO: `skyeye` / `skyeye123`
- Grafana: `admin` / `skyeye123`

## 项目结构

```
skyeye-monitoring-system/
├── skyeye-frontend/          # Vue.js 前端项目
│   ├── src/
│   │   ├── components/       # 组件
│   │   ├── views/           # 页面
│   │   ├── router/          # 路由配置
│   │   └── stores/          # 状态管理
│   └── package.json
├── skyeye-backend/          # Spring Boot 后端项目
│   ├── skyeye-common/       # 通用模块
│   ├── skyeye-gateway/      # API网关
│   ├── skyeye-auth/         # 认证服务
│   └── pom.xml
├── docker/                  # Docker 配置文件
│   ├── postgres/
│   └── prometheus/
├── scripts/                 # 脚本文件
└── docker-compose.yml       # Docker Compose 配置
```

## 数据库设计

系统使用 PostgreSQL 作为主数据库，包含以下主要表：

### 用户管理
- `users` - 用户信息
- `roles` - 角色定义
- `permissions` - 权限定义
- `user_roles` - 用户角色关联
- `role_permissions` - 角色权限关联

### 设备管理
- `device_types` - 设备类型
- `devices` - 设备信息
- `device_groups` - 设备分组
- `device_status_history` - 设备状态历史

### 报警管理
- `alert_types` - 报警类型
- `alert_rules` - 报警规则
- `alerts` - 报警事件
- `notification_channels` - 通知渠道

### 系统管理
- `operation_logs` - 操作日志
- `system_events` - 系统事件
- `system_configurations` - 系统配置
- `scheduled_tasks` - 定时任务

## API 文档

启动服务后，可以通过以下地址访问 API 文档：

- 认证服务 API: http://localhost:8081/swagger-ui.html

## 开发指南

### 代码规范

项目使用以下代码规范工具：

**前端**
- ESLint - JavaScript/TypeScript 代码检查
- Prettier - 代码格式化

**后端**
- Checkstyle - Java 代码规范检查
- SpotBugs - 代码质量分析

### 测试

```bash
# 前端测试
cd skyeye-frontend
npm run test:unit

# 后端测试
cd skyeye-backend
mvn test
```

### 构建部署

```bash
# 前端构建
cd skyeye-frontend
npm run build

# 后端构建
cd skyeye-backend
mvn clean package
```

## 监控和日志

### 应用监控
- Prometheus 收集应用指标
- Grafana 展示监控仪表板
- 访问地址: http://localhost:3001

### 日志管理
- Elasticsearch 存储日志数据
- Kibana 提供日志查询和分析
- 访问地址: http://localhost:5601

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 确保 PostgreSQL 服务正在运行
   - 检查数据库连接配置

2. **Redis 连接失败**
   - 确保 Redis 服务正在运行
   - 检查 Redis 连接配置

3. **前端无法访问后端 API**
   - 检查 API 网关是否正常运行
   - 确认 CORS 配置正确

### 日志查看

```bash
# 查看服务日志
docker-compose logs -f [service-name]

# 查看应用日志
tail -f skyeye-backend/logs/application.log
```

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 项目 Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 邮箱: support@skyeye.com