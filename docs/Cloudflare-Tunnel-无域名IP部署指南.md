# 腾讯云 IP 直连 + Cloudflare Tunnel 后端部署指南

> 适用场景：服务器只有 IP（192.144.136.62），前端已部署在 Cloudflare Pages（https://muying-web.pages.dev），需解决 Mixed Content（HTTPS 前端无法请求 HTTP 后端）问题。

---

## 整体架构

```
用户浏览器
    │
    ▼
https://muying-web.pages.dev  (Cloudflare Pages 前端)
    │  API 请求
    ▼
https://xxxx.trycloudflare.com  (Cloudflare Tunnel，自动 HTTPS)
    │  tunnel 内网穿透
    ▼
192.144.136.62:8080  (腾讯云服务器，Spring Boot 后端 Docker 容器)
```

---

## 一、服务器初始化

### 1.1 登录服务器

```bash
ssh root@192.144.136.62
```

### 1.2 更新系统

```bash
apt update && apt upgrade -y
```

### 1.3 开放防火墙端口（腾讯云安全组）

登录腾讯云控制台 → 云服务器 → 安全组，添加入站规则：

| 协议 | 端口 | 来源 | 说明 |
|------|------|------|------|
| TCP | 22 | 0.0.0.0/0 | SSH |
| TCP | 8080 | 0.0.0.0/0 | 后端 API（cloudflared 本地访问）|

> **注意**：8080 端口仅用于 cloudflared 在服务器本地访问，不需要对公网开放也可以。若只给 cloudflared 使用，可将来源限制为 127.0.0.1。

---

## 二、安装 Docker 和 Docker Compose

```bash
# 卸载旧版本
apt remove -y docker docker-engine docker.io containerd runc 2>/dev/null

# 安装依赖
apt install -y ca-certificates curl gnupg lsb-release

# 添加 Docker GPG 密钥
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

# 添加 Docker 仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
apt update && apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 验证安装
docker --version
docker compose version

# 设置开机自启
systemctl enable docker && systemctl start docker
```

---

## 三、部署后端应用

### 3.1 上传项目代码

**方式A：从本地上传**

```bash
# 在本地 Windows 执行（PowerShell）
scp -r G:/muying/muying-mall root@192.144.136.62:/opt/muying-mall
```

**方式B：使用已有的 tar.gz 包**

```bash
# 上传打包文件
scp G:/muying/muying-mall/muying-mall.tar.gz root@192.144.136.62:/opt/

# 在服务器上解压
mkdir -p /opt/muying-mall
tar -xzf /opt/muying-mall.tar.gz -C /opt/muying-mall
cd /opt/muying-mall
```

### 3.2 创建 .env 配置文件

```bash
cd /opt/muying-mall
cp .env.example .env
nano .env
```

**最小必改项：**

```env
# 数据库密码（修改为强密码）
MYSQL_ROOT_PASSWORD=YourStrongPassword123!

# JWT 密钥（必须修改，建议 64 位以上随机字符串）
JWT_SECRET=your-very-long-random-secret-key-at-least-64-characters-long

# CORS 允许来源（稍后填入 Tunnel 地址，先保持默认）
CORS_ALLOWED_ORIGINS=https://muying-web.pages.dev

# 文件访问域名（稍后填入 Tunnel 地址）
UPLOAD_DOMAIN=http://192.144.136.62:8080
```

### 3.3 创建 application-private.yml

```bash
cat > src/main/resources/application-private.yml << 'EOF'
spring:
  datasource:
    password: YourStrongPassword123!   # 与 MYSQL_ROOT_PASSWORD 保持一致
  data:
    redis:
      password:                         # Redis 无密码留空

jwt:
  secret: your-very-long-random-secret-key-at-least-64-characters-long
EOF
```

### 3.4 创建上传目录

```bash
mkdir -p /opt/muying-mall/uploads/avatars
mkdir -p /opt/muying-mall/logs
```

### 3.5 构建并启动容器

```bash
cd /opt/muying-mall

# 构建镜像并启动所有服务（首次约需 5~10 分钟）
docker compose up -d --build

# 查看启动状态
docker compose ps

# 查看后端日志
docker compose logs -f backend
```

### 3.6 验证后端是否正常

```bash
# 在服务器本地测试
curl http://localhost:8080/api/actuator/health

# 预期输出：
# {"status":"UP",...}
```

---

## 四、安装并配置 Cloudflare Tunnel

### 4.1 安装 cloudflared

```bash
# 下载最新版 cloudflared
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb -o cloudflared.deb
dpkg -i cloudflared.deb

# 验证安装
cloudflared --version
```

### 4.2 创建临时 Tunnel（无需登录，立即可用）

> 此方式使用 `trycloudflare.com` 子域，**无需 Cloudflare 账号**，每次重启地址会变化。适合测试验证。

```bash
# 后台运行，将本地 8080 端口暴露为 HTTPS
nohup cloudflared tunnel --url http://localhost:8080 > /tmp/cloudflared.log 2>&1 &

# 等待 5 秒后查看分配的 HTTPS 地址
sleep 5 && grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' /tmp/cloudflared.log | head -1
```

**记录输出的地址**，例如：`https://example-tunnel-abc123.trycloudflare.com`

### 4.3 创建永久 Tunnel（推荐生产使用）

> 永久 Tunnel 地址固定不变，需要 Cloudflare 账号（免费版即可）。

```bash
# 登录 Cloudflare（会打开浏览器授权，若是纯 SSH 环境，复制链接在本地浏览器打开）
cloudflared tunnel login

# 创建 tunnel（名称自定义，例如 muying-backend）
cloudflared tunnel create muying-backend

# 查看 tunnel ID
cloudflared tunnel list

# 创建路由配置文件
mkdir -p ~/.cloudflared
cat > ~/.cloudflared/config.yml << 'EOF'
tunnel: <你的 TUNNEL_ID>
credentials-file: /root/.cloudflared/<你的 TUNNEL_ID>.json

ingress:
  - service: http://localhost:8080
EOF

# 将 Tunnel 绑定到子域（需要在 Cloudflare 管理的域名，若无域名则跳过此步使用 trycloudflare.com）
# cloudflared tunnel route dns muying-backend api.yourdomain.com

# 启动 Tunnel
nohup cloudflared tunnel run muying-backend > /tmp/cloudflared.log 2>&1 &

# 设置开机自启
cloudflared service install
```

---

## 五、更新 CORS 和文件上传域名配置

拿到 Tunnel 地址后（例如 `https://example-tunnel-abc123.trycloudflare.com`），更新 `.env`：

```bash
nano /opt/muying-mall/.env
```

修改以下两项：

```env
# 同时允许前端 Pages 地址和 Tunnel 后端地址（逗号分隔，无空格）
CORS_ALLOWED_ORIGINS=https://muying-web.pages.dev,https://example-tunnel-abc123.trycloudflare.com

# 文件访问使用 Tunnel 地址（这样前端可以通过 HTTPS 加载上传的图片）
UPLOAD_DOMAIN=https://example-tunnel-abc123.trycloudflare.com
```

重启后端容器使配置生效：

```bash
cd /opt/muying-mall
docker compose restart backend

# 验证新的 CORS 配置
curl -I -X OPTIONS http://localhost:8080/api/user/login \
  -H "Origin: https://muying-web.pages.dev" \
  -H "Access-Control-Request-Method: POST"
```

---

## 六、更新前端 API 地址

前端（Cloudflare Pages）的环境变量需要指向 Tunnel 地址。

在 Cloudflare Pages 控制台 → 你的项目 → Settings → Environment variables，添加：

| 变量名 | 值 |
|--------|----|
| `VITE_API_BASE_URL` | `https://example-tunnel-abc123.trycloudflare.com/api` |

然后触发重新部署。

> 如果前端代码中 API 地址是硬编码的，需要修改源码后重新部署。

---

## 七、常用运维命令

```bash
# 查看所有容器状态
cd /opt/muying-mall && docker compose ps

# 查看后端实时日志
docker compose logs -f backend

# 重启某个服务
docker compose restart backend

# 停止所有服务
docker compose down

# 停止并清除数据卷（危险：会删除数据库数据）
docker compose down -v

# 查看 cloudflared 日志
tail -f /tmp/cloudflared.log

# 查看 Tunnel 状态
cloudflared tunnel info muying-backend
```

---

## 八、故障排查

### 后端无法启动

```bash
# 检查容器日志
docker compose logs backend --tail=100

# 检查 MySQL 是否就绪
docker compose logs mysql --tail=50
```

### CORS 错误

浏览器控制台出现 `Access-Control-Allow-Origin` 错误时：
1. 确认 `.env` 中 `CORS_ALLOWED_ORIGINS` 包含了你的前端地址
2. 重启 backend 容器：`docker compose restart backend`
3. 确认 Tunnel 地址没有尾部斜杠

### Mixed Content 错误

确保前端请求的 API 地址是 `https://` 开头（Tunnel 地址），而不是 `http://IP:8080`。

### Tunnel 地址失效（使用 trycloudflare.com）

临时 Tunnel 每次重启地址会变化，需要：
1. 重新运行 `cloudflared tunnel --url http://localhost:8080`
2. 重新获取地址，更新 `.env` 和前端环境变量
3. 推荐升级为永久 Tunnel（第4.3节）

---

## 九、推荐的生产配置检查清单

- [ ] `MYSQL_ROOT_PASSWORD` 已修改为强密码
- [ ] `JWT_SECRET` 已修改为 64 位以上随机字符串
- [ ] `CORS_ALLOWED_ORIGINS` 仅包含必要的域名
- [ ] `application-private.yml` 中密码与 `.env` 一致
- [ ] Cloudflare Tunnel 使用永久 Tunnel（非 trycloudflare.com 临时地址）
- [ ] 前端 `VITE_API_BASE_URL` 已更新为 Tunnel HTTPS 地址
- [ ] 腾讯云安全组已正确配置
