# 腾讯云 Ubuntu 22.04 后端部署指南

> 母婴商城后端完整部署手册
> 前端已部署于：https://muying-web.pages.dev/ (域名：pig1.de5.net)

---

## 目录

- [一、服务器基础配置](#一服务器基础配置)
- [二、安装 Docker 和 Docker Compose](#二安装-docker-和-docker-compose)
- [三、上传项目代码](#三上传项目代码)
- [四、配置环境变量](#四配置环境变量)
- [五、启动基础设施和后端服务](#五启动基础设施和后端服务)
- [六、部署 AI 客服服务（可选）](#六部署-ai-客服服务可选)
- [七、配置 Nginx 反向代理 + SSL](#七配置-nginx-反向代理--ssl)
- [八、配置域名解析](#八配置域名解析)
- [九、前端 CORS 与后端联调](#九前端-cors-与后端联调)
- [十、常用运维命令](#十常用运维命令)
- [十一、故障排查](#十一故障排查)
- [附录：服务端口一览](#附录服务端口一览)

---

## 一、服务器基础配置

### 1.1 服务器要求

| 项目 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 4 GB | 8 GB |
| 磁盘 | 50 GB SSD | 100 GB SSD |
| 带宽 | 3 Mbps | 5 Mbps |
| 系统 | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS |

> 如果部署 AI 客服服务（Milvus 向量数据库），内存建议 **8GB 以上**。

### 1.2 登录服务器

```bash
ssh root@你的服务器公网IP
```

### 1.3 更新系统 & 安装基础工具

```bash
apt update && apt upgrade -y
apt install -y curl wget git vim unzip htop net-tools ufw
```

### 1.4 配置防火墙

```bash
# 启用防火墙
ufw enable

# 开放必要端口
ufw allow 22      # SSH
ufw allow 80      # HTTP
ufw allow 443     # HTTPS
ufw allow 8080    # 后端 API（调试阶段用，上线后可关闭，走 Nginx 代理）

# 查看状态
ufw status
```

> **安全建议**：生产环境中，MySQL(3306)、Redis(6379)、RabbitMQ(5672/15672) 等端口**不要对外暴露**，只在 Docker 内网通信。

### 1.5 创建部署用户（推荐，非必须）

```bash
adduser deploy
usermod -aG sudo deploy
usermod -aG docker deploy   # 安装 Docker 后再执行
su - deploy
```

---

## 二、安装 Docker 和 Docker Compose

### 2.1 安装 Docker

```bash
# 卸载旧版本（如有）
apt remove -y docker docker-engine docker.io containerd runc 2>/dev/null

# 安装依赖
apt install -y ca-certificates curl gnupg lsb-release

# 添加 Docker 官方 GPG 密钥
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

# 添加 Docker 仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 验证安装
docker --version
docker compose version
```

### 2.2 配置 Docker 镜像加速（国内服务器建议）

```bash
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF

# 重启 Docker
systemctl daemon-reload
systemctl restart docker
systemctl enable docker
```

### 2.3 验证 Docker

```bash
docker run hello-world
```

---

## 三、上传项目代码

### 方式一：Git 拉取（推荐）

```bash
mkdir -p /opt/muying
cd /opt/muying

# 克隆项目（替换为你的仓库地址）
git clone https://你的仓库地址/muying-mall.git
git clone https://你的仓库地址/muying-ai-service.git  # 可选
```

### 方式二：本地打包上传

在本地 Windows 机器上执行：

```bash
# 打包项目（排除不必要的文件）
cd G:\muying
tar --exclude='*/node_modules' --exclude='*/target' --exclude='*/.git' -czf muying-mall.tar.gz muying-mall/

# 上传到服务器
scp muying-mall.tar.gz root@你的服务器IP:/opt/muying/
```

在服务器上解压：

```bash
cd /opt/muying
tar -xzf muying-mall.tar.gz
```

### 方式三：SFTP 工具上传

使用 WinSCP、FileZilla 或 MobaXterm 连接服务器，将以下文件/目录上传到 `/opt/muying/muying-mall/`：

必须上传的文件：
```
muying-mall/
├── src/                    # 源代码
├── pom.xml                 # Maven 配置
├── Dockerfile              # Docker 构建文件
├── docker-compose.yml      # Docker Compose 编排
├── .env.example            # 环境变量模板
├── muying_mall.sql         # 数据库初始化脚本
└── .mvn/                   # Maven Wrapper
```

---

## 四、配置环境变量

### 4.1 创建 .env 文件

```bash
cd /opt/muying/muying-mall
cp .env.example .env
vim .env
```

### 4.2 修改 .env 配置

```bash
# ==================== 端口配置 ====================
MYSQL_PORT=3306
REDIS_PORT=6379
RABBITMQ_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672
BACKEND_PORT=8080

# ==================== 数据库配置 ====================
# ⚠️ 生产环境务必修改为强密码！
MYSQL_ROOT_PASSWORD=YourStrongPassword123!

# ==================== Redis配置 ====================
REDIS_PASSWORD=YourRedisPassword456!

# ==================== RabbitMQ配置 ====================
RABBITMQ_USER=muying_admin
RABBITMQ_PASSWORD=YourRabbitPassword789!

# ==================== Spring配置 ====================
SPRING_PROFILES_ACTIVE=prod

# ==================== JWT配置 ====================
# ⚠️ 生产环境务必修改！建议使用 64 字节以上的随机字符串
JWT_SECRET=这里替换为一个足够长且随机的密钥字符串

# ==================== 支付宝配置 ====================
ALIPAY_APP_ID=你的支付宝AppID
ALIPAY_PRIVATE_KEY=你的支付宝私钥
ALIPAY_PUBLIC_KEY=你的支付宝公钥
```

> **生成随机 JWT 密钥：**
> ```bash
> openssl rand -base64 64
> ```

### 4.3 创建生产环境 application-private.yml

在服务器上需要确保 `application-private.yml` 中的配置与 `.env` 一致，Docker 环境变量会自动覆盖 Spring 配置，但建议保持一致。

### 4.4 修改生产环境配置

编辑 `src/main/resources/application-prod.properties`：

```properties
# CORS配置 - 生产环境
cors.allowed-origins=https://muying-web.pages.dev,https://pig1.de5.net

# 文件上传配置 - Linux 服务器路径
upload.path=/opt/muying/uploads
upload.avatar.path=/avatars
upload.domain=https://api.pig1.de5.net

# 文件访问URL
file.upload.path=/opt/muying/uploads
file.access.url=https://api.pig1.de5.net/avatars

# 文件上传大小限制
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

创建上传目录：

```bash
mkdir -p /opt/muying/uploads/avatars
chmod 755 /opt/muying/uploads
```

---

## 五、启动基础设施和后端服务

### 5.1 修改 docker-compose.yml 安全配置

编辑 `docker-compose.yml`，为 Redis 添加密码：

```yaml
  redis:
    # ... 其他配置不变 ...
    command: >
      redis-server
      --appendonly yes
      --requirepass ${REDIS_PASSWORD:-}
      --maxmemory 1gb
      --maxmemory-policy volatile-lru
      --notify-keyspace-events Ex
```

### 5.2 构建并启动所有服务

```bash
cd /opt/muying/muying-mall

# 构建后端镜像并启动所有服务
docker compose up -d --build
```

> 首次构建需要下载依赖，可能需要 **10-20 分钟**，请耐心等待。

### 5.3 查看启动状态

```bash
# 查看所有容器状态
docker compose ps

# 期望输出（所有状态应为 healthy 或 running）：
# NAME              STATUS              PORTS
# muying-mysql      Up (healthy)        0.0.0.0:3306->3306/tcp
# muying-redis      Up (healthy)        0.0.0.0:6379->6379/tcp
# muying-rabbitmq   Up (healthy)        0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
# muying-backend    Up (healthy)        0.0.0.0:8080->8080/tcp
```

### 5.4 查看后端启动日志

```bash
# 查看后端日志（等待 Spring Boot 完全启动）
docker compose logs -f backend

# 看到以下内容说明启动成功：
# Started MuyingMallApplication in XX seconds
```

### 5.5 验证后端服务

```bash
# 健康检查
curl http://localhost:8080/api/actuator/health

# 期望输出：{"status":"UP", ...}
```

---

## 六、部署 AI 客服服务（可选）

> AI 客服依赖 Milvus 向量数据库，内存消耗较大。服务器内存 < 8GB 可跳过此步。

### 6.1 上传 AI 服务代码

```bash
# 将 muying-ai-service 上传到 /opt/muying/muying-ai-service/
```

### 6.2 配置 AI 服务环境变量

```bash
cd /opt/muying/muying-ai-service

# 创建 .env 文件
cat > .env << 'EOF'
# AI 模型 API Key（至少配置一个）
DEEPSEEK_API_KEY=你的DeepSeek-API-Key
QIANWEN_API_KEY=你的通义千问-API-Key
ZHIPU_API_KEY=你的智谱-API-Key
EOF
```

### 6.3 修改 AI 服务配置

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
# Redis 复用主服务的 Redis
spring:
  data:
    redis:
      host: muying-redis      # 使用 Docker 容器名
      port: 6379

# Milvus 连接
spring.ai:
  vectorstore:
    milvus:
      client:
        host: milvus           # 使用 Docker 容器名
        port: 19530

# 母婴商城 API 地址
mall:
  api:
    base-url: http://muying-backend:8080/api  # 使用 Docker 容器名

# CORS 配置
web:
  cors:
    allowed-origins: "https://muying-web.pages.dev,https://pig1.de5.net"
```

### 6.4 启动 AI 服务

```bash
cd /opt/muying/muying-ai-service

# 启动 Milvus 及依赖
docker compose up -d

# 如果 AI 服务也有 Dockerfile，则构建并启动
# docker build -t muying-ai-service .
# docker run -d --name muying-ai-service \
#   --network muying-mall_muying-network \
#   -p 8090:8090 \
#   -e DEEPSEEK_API_KEY=你的Key \
#   muying-ai-service
```

---

## 七、配置 Nginx 反向代理 + SSL

### 7.1 安装 Nginx

```bash
apt install -y nginx
systemctl enable nginx
```

### 7.2 安装 Certbot（Let's Encrypt 免费 SSL）

```bash
apt install -y certbot python3-certbot-nginx
```

### 7.3 配置 Nginx

```bash
cat > /etc/nginx/sites-available/muying-api << 'NGINX'
# ============================================
# 母婴商城后端 API 反向代理配置
# 域名：api.pig1.de5.net
# ============================================

server {
    listen 80;
    server_name api.pig1.de5.net;

    # Certbot 验证用
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # HTTP 重定向到 HTTPS
    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name api.pig1.de5.net;

    # SSL 证书路径（Certbot 自动管理）
    ssl_certificate /etc/letsencrypt/live/api.pig1.de5.net/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.pig1.de5.net/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 请求体大小限制
    client_max_body_size 50M;

    # Gzip 压缩
    gzip on;
    gzip_types application/json application/xml text/plain text/css application/javascript;
    gzip_min_length 1024;

    # ---- 主后端 API ----
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持（如有需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # ---- AI 客服 API（可选）----
    location /ai/ {
        proxy_pass http://127.0.0.1:8090/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # SSE 流式响应支持
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 300s;
    }

    # ---- 静态文件（上传的图片等）----
    location /avatars/ {
        alias /opt/muying/uploads/avatars/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # ---- 健康检查 ----
    location /health {
        proxy_pass http://127.0.0.1:8080/api/actuator/health;
        access_log off;
    }
}
NGINX
```

### 7.4 启用站点配置

```bash
# 创建软链接
ln -s /etc/nginx/sites-available/muying-api /etc/nginx/sites-enabled/

# 删除默认站点（可选）
rm -f /etc/nginx/sites-enabled/default

# 测试配置语法
nginx -t

# 重载 Nginx
systemctl reload nginx
```

### 7.5 申请 SSL 证书

> 先完成域名解析（第八节），确保 `api.pig1.de5.net` 已指向服务器 IP，再执行此步。

```bash
# 先用 HTTP 配置启动 Nginx（注释掉 443 server 块）
# 然后申请证书
certbot --nginx -d api.pig1.de5.net

# Certbot 会自动修改 Nginx 配置并添加 SSL
# 自动续期已默认配置，无需手动操作

# 验证自动续期
certbot renew --dry-run
```

**如果使用 Cloudflare 代理（橙色云朵），则无需 Certbot**，Cloudflare 会自动处理 SSL。此时 Nginx 只需监听 80 端口即可：

```bash
cat > /etc/nginx/sites-available/muying-api << 'NGINX'
server {
    listen 80;
    server_name api.pig1.de5.net;

    client_max_body_size 50M;

    gzip on;
    gzip_types application/json application/xml text/plain text/css application/javascript;
    gzip_min_length 1024;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /ai/ {
        proxy_pass http://127.0.0.1:8090/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 300s;
    }

    location /avatars/ {
        alias /opt/muying/uploads/avatars/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location /health {
        proxy_pass http://127.0.0.1:8080/api/actuator/health;
        access_log off;
    }
}
NGINX
```

---

## 八、配置域名解析

### 方案 A：直接 DNS 解析（推荐简单场景）

在你的域名 DNS 管理后台（Cloudflare 等），添加以下记录：

| 类型 | 名称 | 内容 | 代理状态 |
|------|------|------|----------|
| A | `api` | 你的腾讯云服务器公网 IP | 可开启 Cloudflare 代理 |

解析生效后，`https://api.pig1.de5.net` 即可访问后端 API。

### 方案 B：Cloudflare Tunnel（推荐，无需公网端口）

如果你已经在使用 Cloudflare，可以用 Tunnel 方式，无需开放 80/443 端口：

```bash
# 安装 cloudflared
curl -L --output cloudflared.deb https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
dpkg -i cloudflared.deb

# 登录 Cloudflare
cloudflared tunnel login

# 创建隧道
cloudflared tunnel create muying-api

# 配置隧道
cat > ~/.cloudflared/config.yml << 'EOF'
tunnel: <你的隧道ID>
credentials-file: /root/.cloudflared/<你的隧道ID>.json

ingress:
  - hostname: api.pig1.de5.net
    service: http://localhost:8080
  - service: http_status:404
EOF

# 配置 DNS
cloudflared tunnel route dns muying-api api.pig1.de5.net

# 启动隧道（前台测试）
cloudflared tunnel run muying-api

# 设置为系统服务
cloudflared service install
systemctl enable cloudflared
systemctl start cloudflared
```

---

## 九、前端 CORS 与后端联调

### 9.1 确认后端 CORS 配置

确保 `application.yml` 中的 CORS 白名单包含前端域名：

```yaml
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000,https://muying-web.pages.dev,https://pig1.de5.net,https://api.pig1.de5.net
```

### 9.2 前端 API 地址配置

前端项目 `muying-web` 的 `.env.production`（或 Cloudflare Pages 环境变量）中配置：

```bash
VITE_API_BASE_URL=https://api.pig1.de5.net/api
VITE_IMAGE_BASE_URL=https://api.pig1.de5.net
```

### 9.3 验证联调

```bash
# 在本地浏览器或服务器上测试
curl -H "Origin: https://muying-web.pages.dev" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     https://api.pig1.de5.net/api/actuator/health

# 应返回 200 并包含 CORS 响应头
```

---

## 十、常用运维命令

### 10.1 服务管理

```bash
cd /opt/muying/muying-mall

# 查看所有容器状态
docker compose ps

# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 重启后端（不重启数据库）
docker compose restart backend

# 重新构建并启动后端
docker compose up -d --build backend

# 查看实时日志
docker compose logs -f backend
docker compose logs -f mysql
docker compose logs -f redis
```

### 10.2 数据库操作

```bash
# 进入 MySQL 容器
docker compose exec mysql mysql -u root -p

# 备份数据库
docker compose exec mysql mysqldump -u root -p muying_mall > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker compose exec -T mysql mysql -u root -p muying_mall < backup.sql
```

### 10.3 监控

```bash
# 查看容器资源占用
docker stats

# 查看磁盘使用
df -h

# 查看内存使用
free -h

# 查看后端日志（最近 100 行）
docker compose logs --tail=100 backend
```

### 10.4 更新部署

```bash
cd /opt/muying/muying-mall

# 拉取最新代码
git pull origin main

# 重新构建并启动（只重启后端，不影响数据库）
docker compose up -d --build backend
```

### 10.5 定时备份（crontab）

```bash
crontab -e

# 添加每天凌晨 3 点自动备份数据库
0 3 * * * cd /opt/muying/muying-mall && docker compose exec -T mysql mysqldump -u root -pYourStrongPassword123! muying_mall | gzip > /opt/muying/backups/muying_mall_$(date +\%Y\%m\%d).sql.gz
```

```bash
mkdir -p /opt/muying/backups
```

---

## 十一、故障排查

### 问题 1：后端启动失败

```bash
# 查看后端详细日志
docker compose logs backend

# 常见原因：
# 1. MySQL 未就绪 → 等待 health check 通过后重启 backend
docker compose restart backend

# 2. 内存不足 → 调整 JVM 参数
# 在 docker-compose.yml 中修改 JAVA_OPTS
JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"
```

### 问题 2：数据库连接失败

```bash
# 检查 MySQL 是否正常运行
docker compose ps mysql
docker compose logs mysql

# 测试 MySQL 连接
docker compose exec mysql mysql -u root -p -e "SELECT 1;"
```

### 问题 3：Redis 连接失败

```bash
# 检查 Redis
docker compose exec redis redis-cli ping
# 有密码的情况
docker compose exec redis redis-cli -a YourRedisPassword456! ping
```

### 问题 4：前端跨域报错

```bash
# 检查 CORS 配置是否正确
curl -v -H "Origin: https://muying-web.pages.dev" \
     https://api.pig1.de5.net/api/actuator/health 2>&1 | grep -i "access-control"

# 确认响应头包含：
# Access-Control-Allow-Origin: https://muying-web.pages.dev
# Access-Control-Allow-Credentials: true
```

### 问题 5：Docker 构建缓慢

```bash
# 清理 Docker 缓存
docker system prune -a

# 使用国内 Maven 镜像加速构建
# 在 pom.xml 或 settings.xml 中配置阿里云镜像
```

---

## 附录：服务端口一览

| 服务 | 容器端口 | 宿主机端口 | 说明 |
|------|----------|------------|------|
| MySQL | 3306 | 3306 | 主数据库 |
| Redis | 6379 | 6379 | 缓存 & Session |
| RabbitMQ | 5672 | 5672 | 消息队列 |
| RabbitMQ 管理界面 | 15672 | 15672 | Web 管理 |
| 后端 API | 8080 | 8080 | Spring Boot |
| AI 客服 | 8090 | 8090 | Spring Boot AI |
| Milvus | 19530 | 19530 | 向量数据库 |
| Nginx | 80/443 | 80/443 | 反向代理 |

## 附录：完整部署流程总结

```
1. 登录服务器 → 更新系统 → 安装基础工具 → 配置防火墙
2. 安装 Docker → 配置镜像加速
3. 上传代码到 /opt/muying/muying-mall/
4. 配置 .env → 修改 application-prod.properties
5. docker compose up -d --build（等待所有服务启动）
6. 安装 Nginx → 配置反向代理
7. 域名解析 api.pig1.de5.net → 服务器 IP
8. 申请 SSL 证书（或使用 Cloudflare 代理）
9. 前端配置 VITE_API_BASE_URL=https://api.pig1.de5.net/api
10. 测试联调 → 完成部署
```
