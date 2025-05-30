# 母婴商城Docker部署指南

本文档详细描述了如何使用Docker和Docker Compose来部署母婴商城项目，包括环境准备、镜像构建、容器配置及服务编排等内容。

## 目录

- [环境准备](#环境准备)
- [项目结构](#项目结构)
- [Docker镜像构建](#docker镜像构建)
- [Docker Compose服务编排](#docker-compose服务编排)
- [部署步骤](#部署步骤)
- [监控和管理](#监控和管理)
- [常见问题](#常见问题)
- [附录](#附录)

## 环境准备

### 前提条件

在开始部署前，确保已安装以下软件：

- Docker Engine (20.10.x 或更高版本)
- Docker Compose (2.x 或更高版本)
- Git (用于拉取代码仓库)

### 系统要求

| 环境 | 最低配置 | 推荐配置 |
|------|---------|---------|
| 开发/测试 | 2核CPU, 4GB内存, 50GB存储 | 4核CPU, 8GB内存, 100GB SSD |
| 生产 | 4核CPU, 8GB内存, 100GB SSD | 8核CPU, 16GB内存, 200GB SSD |

### 网络要求

- 为容器分配静态IP或使用Docker网络
- 确保以下端口可用：
  - 8080: API服务
  - 3306: MySQL
  - 6379: Redis
  - 9200, 9300: Elasticsearch
  - 9000, 9001: MinIO
  - 5672, 15672: RabbitMQ

## 项目结构

Docker部署相关的文件位于项目根目录的`/docker`文件夹下：

```
muying-mall/
├── docker/
│   ├── Dockerfile                # 主应用Dockerfile
│   ├── docker-compose.yml        # 生产环境Docker Compose配置
│   ├── docker-compose-dev.yml    # 开发环境Docker Compose配置
│   ├── nginx/                    # Nginx配置
│   │   ├── conf/
│   │   │   └── nginx.conf
│   │   └── html/
│   ├── mysql/
│   │   └── initdb/              # MySQL初始化脚本
│   └── elasticsearch/
│       └── config/              # Elasticsearch配置
└── ...
```

## Docker镜像构建

### 主应用镜像

项目使用多阶段构建来生成高效的Docker镜像。以下是`Dockerfile`的详细说明：

```dockerfile
# 构建阶段
FROM maven:3.8.5-openjdk-21 as build

# 设置工作目录
WORKDIR /app

# 复制pom.xml
COPY pom.xml .

# 下载依赖项
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn package -DskipTests

# 运行阶段
FROM openjdk:21-slim

WORKDIR /app

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms512m -Xmx1024m"

# 从构建阶段复制JAR文件
COPY --from=build /app/target/*.jar app.jar

# 暴露API端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 构建镜像

使用以下命令构建主应用镜像：

```bash
# 进入项目根目录
cd muying-mall

# 构建Docker镜像
docker build -t muying-mall:latest -f docker/Dockerfile .
```

您也可以为镜像添加版本标签：

```bash
docker build -t muying-mall:1.0.0 -f docker/Dockerfile .
```

## Docker Compose服务编排

项目使用Docker Compose来编排多个服务，包括主应用、数据库、缓存、搜索引擎等。

### 生产环境配置

以下是`docker-compose.yml`文件的详细说明：

```yaml
version: '3.8'

services:
  # 主应用服务
  app:
    image: muying-mall:latest
    container_name: muying-mall-app
    restart: always
    depends_on:
      - mysql
      - redis
      - elasticsearch
      - minio
      - rabbitmq
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/muying_mall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=rootpassword
      - SPRING_REDIS_HOST=redis
      - SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200
      - MINIO_ENDPOINT=http://minio:9000
      - SPRING_RABBITMQ_HOST=rabbitmq
    volumes:
      - app-logs:/app/logs
    networks:
      - muying-net
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  # Nginx服务（反向代理和静态资源）
  nginx:
    image: nginx:1.21-alpine
    container_name: muying-mall-nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/conf/nginx.conf:/etc/nginx/nginx.conf
      - ./docker/nginx/html:/usr/share/nginx/html
      - ./docker/nginx/ssl:/etc/nginx/ssl
    depends_on:
      - app
    networks:
      - muying-net

  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: muying-mall-mysql
    restart: always
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=muying_mall
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/mysql/initdb:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    networks:
      - muying-net

  # Redis缓存
  redis:
    image: redis:6.2-alpine
    container_name: muying-mall-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: ["redis-server", "--appendonly", "yes", "--requirepass", "redispassword"]
    networks:
      - muying-net

  # Elasticsearch搜索引擎
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: muying-mall-elasticsearch
    restart: always
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
      - ./docker/elasticsearch/config:/usr/share/elasticsearch/config
    ulimits:
      memlock:
        soft: -1
        hard: -1
    networks:
      - muying-net

  # MinIO对象存储
  minio:
    image: minio/minio
    container_name: muying-mall-minio
    restart: always
    ports:
      - "9000:9000"  # API端口
      - "9001:9001"  # 控制台端口
    environment:
      - MINIO_ROOT_USER=minioadmin
      - MINIO_ROOT_PASSWORD=minioadmin
    volumes:
      - minio-data:/data
    command: server /data --console-address ":9001"
    networks:
      - muying-net

  # RabbitMQ消息队列
  rabbitmq:
    image: rabbitmq:3.9-management-alpine
    container_name: muying-mall-rabbitmq
    restart: always
    ports:
      - "5672:5672"  # AMQP端口
      - "15672:15672"  # 管理界面端口
    environment:
      - RABBITMQ_DEFAULT_USER=guest
      - RABBITMQ_DEFAULT_PASS=guest
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - muying-net

# 定义卷
volumes:
  app-logs:
  mysql-data:
  redis-data:
  elasticsearch-data:
  minio-data:
  rabbitmq-data:

# 定义网络
networks:
  muying-net:
    driver: bridge
```

### 开发环境配置

开发环境使用的是简化版的Docker Compose配置，主要包含依赖服务：

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: muying-mall-mysql-dev
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=muying_mall
    volumes:
      - mysql-data-dev:/var/lib/mysql
    networks:
      - muying-net-dev

  # Redis缓存
  redis:
    image: redis:6.2-alpine
    container_name: muying-mall-redis-dev
    ports:
      - "6379:6379"
    networks:
      - muying-net-dev

  # Elasticsearch搜索引擎
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: muying-mall-elasticsearch-dev
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    networks:
      - muying-net-dev

  # MinIO对象存储
  minio:
    image: minio/minio
    container_name: muying-mall-minio-dev
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      - MINIO_ROOT_USER=minioadmin
      - MINIO_ROOT_PASSWORD=minioadmin
    volumes:
      - minio-data-dev:/data
    command: server /data --console-address ":9001"
    networks:
      - muying-net-dev

volumes:
  mysql-data-dev:
  minio-data-dev:

networks:
  muying-net-dev:
    driver: bridge
```

## 部署步骤

### 1. 克隆代码仓库

```bash
git clone https://github.com/yourusername/muying-mall.git
cd muying-mall
```

### 2. 配置环境变量

创建`.env`文件（与docker-compose.yml同级），用于存储敏感信息：

```
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=muying_mall
MYSQL_USER=muying_user
MYSQL_PASSWORD=your_secure_db_password

# Redis配置
REDIS_PASSWORD=your_secure_redis_password

# MinIO配置
MINIO_ROOT_USER=your_minio_admin
MINIO_ROOT_PASSWORD=your_secure_minio_password

# 应用配置
SPRING_PROFILES_ACTIVE=prod
APP_JAVA_OPTS=-Xms512m -Xmx1024m
```

### 3. 构建应用镜像

```bash
docker build -t muying-mall:latest -f docker/Dockerfile .
```

### 4. 启动服务

生产环境部署：

```bash
docker-compose -f docker/docker-compose.yml up -d
```

开发环境部署：

```bash
docker-compose -f docker/docker-compose-dev.yml up -d
```

### 5. 初始化数据库

如果需要初始化数据库（第一次部署时）：

```bash
# 等待MySQL容器启动完成
sleep 10

# 导入初始数据
docker exec -i muying-mall-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} muying_mall < main.sql
```

### 6. 验证部署

访问以下URL验证各服务是否正常运行：

- 主应用API: http://your-server-ip:8080/api/swagger-ui.html
- 主应用健康检查: http://your-server-ip:8080/actuator/health
- Elasticsearch: http://your-server-ip:9200
- MinIO控制台: http://your-server-ip:9001
- RabbitMQ管理界面: http://your-server-ip:15672

## 扩展部署

### 集群部署

在生产环境中，可以使用Docker Swarm或Kubernetes进行集群部署。以下是使用Docker Swarm的简单示例：

1. 初始化Swarm集群：

```bash
docker swarm init --advertise-addr <MANAGER-IP>
```

2. 创建Docker Stack配置文件（muying-stack.yml）：

```yaml
version: '3.8'

services:
  app:
    image: muying-mall:latest
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 10s
      restart_policy:
        condition: on-failure
    # ... 其他配置与docker-compose.yml相同
```

3. 部署Stack：

```bash
docker stack deploy -c muying-stack.yml muying-mall
```

### 蓝绿部署

蓝绿部署可以实现零停机更新：

1. 假设当前运行的是"蓝色"版本：

```bash
docker-compose -f docker/docker-compose-blue.yml up -d
```

2. 构建并部署"绿色"版本：

```bash
docker build -t muying-mall:green -f docker/Dockerfile .
docker-compose -f docker/docker-compose-green.yml up -d
```

3. 测试"绿色"版本后，更新Nginx配置将流量切换到"绿色"版本

4. 确认"绿色"版本运行正常后，停止"蓝色"版本：

```bash
docker-compose -f docker/docker-compose-blue.yml down
```

## 监控和管理

### 日志收集

可以使用ELK（Elasticsearch, Logstash, Kibana）或EFK（Elasticsearch, Fluentd, Kibana）进行日志收集和分析：

```yaml
# 添加到docker-compose.yml
  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    volumes:
      - ./docker/logstash/config:/usr/share/logstash/config
    networks:
      - muying-net

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    networks:
      - muying-net
```

### 监控系统

使用Prometheus和Grafana进行系统和应用监控：

```yaml
# 添加到docker-compose.yml
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/config:/etc/prometheus
      - prometheus-data:/prometheus
    networks:
      - muying-net

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
    networks:
      - muying-net
```

### 容器管理

可以使用Portainer进行容器可视化管理：

```yaml
# 添加到docker-compose.yml
  portainer:
    image: portainer/portainer-ce
    ports:
      - "9000:9000"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - portainer-data:/data
    networks:
      - muying-net
```

## 常见问题

### 1. 容器无法启动

检查以下几点：
- 查看容器日志：`docker logs <container_id>`
- 确认端口没有冲突
- 检查磁盘空间是否足够
- 验证配置文件格式是否正确

### 2. 应用无法连接数据库

可能的解决方案：
- 确认数据库容器已启动：`docker ps | grep mysql`
- 检查数据库连接参数是否正确
- 验证网络配置，确保容器间可以通信

### 3. Elasticsearch内存不足

Elasticsearch默认需要较大内存，可以调整JVM配置：

```yaml
elasticsearch:
  environment:
    - "ES_JAVA_OPTS=-Xms256m -Xmx256m"  # 减少内存使用
```

### 4. 镜像构建失败

常见原因：
- Maven构建失败，检查项目代码是否有错误
- 网络连接问题，确保能访问Maven仓库
- 构建上下文过大，优化.dockerignore文件

## 备份与恢复

### 数据库备份

创建一个定时任务，定期备份MySQL数据：

```bash
# 创建备份脚本
cat > backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"

# 创建备份目录
mkdir -p $BACKUP_DIR

# MySQL备份
docker exec muying-mall-mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --all-databases' > $BACKUP_DIR/mysql_${DATE}.sql

# 压缩备份
gzip $BACKUP_DIR/mysql_${DATE}.sql

# 删除30天前的备份
find $BACKUP_DIR -name "mysql_*.sql.gz" -type f -mtime +30 -delete
EOF

# 设置执行权限
chmod +x backup.sh

# 添加到crontab
(crontab -l 2>/dev/null; echo "0 3 * * * /path/to/backup.sh") | crontab -
```

### 数据恢复

从备份恢复MySQL数据：

```bash
# 解压备份文件
gunzip mysql_20231201_030000.sql.gz

# 恢复数据
docker exec -i muying-mall-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < mysql_20231201_030000.sql
```

## 附录

### Docker Compose命令备忘录

```bash
# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d

# 停止所有服务
docker-compose -f docker/docker-compose.yml down

# 查看服务状态
docker-compose -f docker/docker-compose.yml ps

# 查看服务日志
docker-compose -f docker/docker-compose.yml logs -f

# 重启特定服务
docker-compose -f docker/docker-compose.yml restart app

# 更新特定服务（先构建新镜像）
docker-compose -f docker/docker-compose.yml up -d --no-deps app

# 查看服务资源使用情况
docker stats
```

### 安全最佳实践

1. **敏感信息处理**
   - 使用.env文件存储敏感信息
   - 不要将密码等敏感信息提交到代码仓库
   - 考虑使用Docker Secrets或环境变量管理工具

2. **镜像安全**
   - 使用官方镜像作为基础
   - 定期更新镜像以修复安全漏洞
   - 使用镜像扫描工具检查漏洞

3. **容器安全**
   - 以非root用户运行容器
   - 限制容器资源使用
   - 为容器添加只读文件系统（只需要写入指定卷）

### 参考资源

- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [Spring Boot Docker指南](https://spring.io/guides/topicals/spring-boot-docker/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)
- [Redis Docker Hub](https://hub.docker.com/_/redis)
- [Elasticsearch Docker文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html)
- [MinIO Docker文档](https://docs.min.io/docs/minio-docker-quickstart-guide.html) 