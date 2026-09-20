# 🎵 Music Manager | 智能音乐管理系统

一个基于 Spring Boot 4 + AI 视觉识别的音乐媒体库管理系统，支持通过专辑封面图片自动识别歌手与专辑信息并写入数据库。

## ✨ 项目亮点

- **AI 视觉识别自动建档**：上传专辑封面图片，调用 DeepSeek 多模态视觉大模型（`deepseek-v4-flash-vision-exp`）自动识别歌手、专辑名，一步完成数据录入，减少人工录入成本
- **多租户级微服务经验迁移**：技术栈与作者另一项目「受限空间登记系统」（BladeX 微服务架构）保持一致，形成完整的 Java 后端能力闭环
- **容器化开发环境**：MySQL / Redis / Nacos 全部通过 Docker 容器化部署，环境一致性好，便于快速迁移和演示

## 🛠 技术栈

| 类别 | 技术选型 |
|------|---------|
| 后端框架 | Spring Boot 4.1.1 |
| ORM 框架 | MyBatis-Plus 3.5.15 |
| 数据库 | MySQL 8.0（Docker 部署） |
| AI 能力 | DeepSeek 视觉大模型（`deepseek-v4-flash-vision-exp`） |
| HTTP 客户端 | OkHttp 4.12.0 |
| JSON 处理 | Jackson |
| 构建工具 | Maven 3.9.16 |
| 开发环境 | JDK 17、IntelliJ IDEA、Docker Desktop |

## 📦 核心功能

### 基础 CRUD
- 歌曲信息的增删改查（`/song/list`、`/song/add`、`/song/update`、`/song/delete/{id}`）

### AI 封面识别
- `POST /song/recognize-cover`：上传封面图片，返回 AI 识别出的歌手、专辑名（JSON 格式）
- `POST /song/recognize-and-add`：识别 + 自动写入数据库，一步完成"上传即建档"

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- Docker Desktop（用于运行 MySQL）
- DeepSeek API Key（[platform.deepseek.com](https://platform.deepseek.com) 申请）

### 启动步骤

```bash
# 1. 启动 MySQL 容器
docker run -d --name mysql-dev -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0

# 2. 创建数据库
docker exec -it mysql-dev mysql -uroot -p123456 -e "CREATE DATABASE music_manager DEFAULT CHARACTER SET utf8mb4;"

# 3. 配置环境变量（DeepSeek API Key）
# Windows: 系统属性 → 环境变量 → 新建 DEEPSEEK_API_KEY

# 4. 运行项目
mvn spring-boot:run
```

### 接口测试示例

```bash
# 上传封面图片，识别并自动建档
curl -X POST http://localhost:8080/song/recognize-and-add \
  -F "file=@/path/to/cover.jpg"
```

## 🔒 安全说明

项目中的 API Key 通过系统环境变量注入（`${DEEPSEEK_API_KEY}`），不在代码库中硬编码，避免密钥泄露。

## 📮 联系方式

作者：钱京国 | 皖江工学院计算机专业
