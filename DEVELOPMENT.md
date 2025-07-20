# SkyEye 智能监控系统 - 开发指南

## 🚀 快速开始

### 环境要求
- Node.js 18+
- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Redis 6+

### 项目结构
```
skyeye/
├── skyeye-frontend/          # Vue 3 前端项目
├── skyeye-backend/           # Spring Boot 后端项目
├── docker/                   # Docker 配置文件
├── prototype/                # 原型设计文件
└── scripts/                  # 部署和工具脚本
```

## 🔧 开发环境配置

### 1. 前端开发环境

```bash
cd skyeye-frontend

# 安装依赖
npm install

# 复制环境变量文件
cp .env.example .env.local

# 编辑 .env.local 文件，配置实际的API地址等信息

# 启动开发服务器
npm run dev
```

访问地址：http://localhost:3000

### 2. 后端开发环境

```bash
cd skyeye-backend

# 复制环境变量文件
cp .env.example .env

# 编辑 .env 文件，配置数据库连接等信息

# 启动后端服务
mvn spring-boot:run
```

API地址：http://localhost:8080

### 3. 数据库配置

#### PostgreSQL
```sql
-- 创建数据库
CREATE DATABASE skyeye;

-- 创建用户
CREATE USER skyeye_user WITH PASSWORD 'your_password';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE skyeye TO skyeye_user;
```

#### Redis
```bash
# 启动Redis服务
redis-server

# 或使用Docker
docker run -d -p 6379:6379 redis:alpine
```

## 🧪 测试

### 前端测试
```bash
cd skyeye-frontend

# 单元测试
npm run test:unit

# E2E测试
npm run test:e2e

# 测试覆盖率
npm run coverage
```

### 后端测试
```bash
cd skyeye-backend

# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=UserServiceTest

# 生成测试报告
mvn surefire-report:report
```

## 📦 构建和部署

### 前端构建
```bash
cd skyeye-frontend
npm run build
```

### 后端构建
```bash
cd skyeye-backend
mvn clean package
```

### Docker部署
```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d
```

## 🔐 安全配置

### 环境变量安全
- 永远不要将 `.env` 文件提交到版本控制
- 使用 `.env.example` 作为模板
- 生产环境使用强密码和密钥

### 数据库安全
- 使用专用数据库用户
- 限制数据库访问权限
- 定期备份数据

## 📝 开发规范

### Git提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

### 代码规范
- 前端：使用ESLint + Prettier
- 后端：遵循Google Java Style Guide
- 提交前运行代码检查

## 🐛 常见问题

### 1. 前端启动失败
- 检查Node.js版本是否符合要求
- 删除node_modules重新安装
- 检查端口是否被占用

### 2. 后端连接数据库失败
- 检查数据库服务是否启动
- 验证连接配置是否正确
- 检查防火墙设置

### 3. 跨域问题
- 检查后端CORS配置
- 确认前端API地址配置正确

## 📞 技术支持

如有问题，请：
1. 查看本文档的常见问题部分
2. 检查项目的Issue列表
3. 联系开发团队

## 📄 许可证

本项目采用 MIT 许可证，详见 LICENSE 文件。