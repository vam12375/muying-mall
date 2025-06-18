# 母婴商城部署架构设计

## 概述

本文档描述了母婴商城系统在不同环境下的部署架构设计，包括开发环境、测试环境和生产环境的部署方案，以及容器化、监控、日志等基础设施的配置。

## 部署架构总览

```mermaid
graph TB
    subgraph "用户层"
        U1[移动端用户]
        U2[Web端用户]
        U3[管理员用户]
    end
    
    subgraph "CDN层"
        CDN[CDN节点<br/>静态资源分发]
    end
    
    subgraph "负载均衡层"
        LB1[主负载均衡器<br/>Nginx/HAProxy]
        LB2[备负载均衡器<br/>Nginx/HAProxy]
    end
    
    subgraph "API网关层"
        GW1[API Gateway 1<br/>Spring Cloud Gateway]
        GW2[API Gateway 2<br/>Spring Cloud Gateway]
        GW3[API Gateway 3<br/>Spring Cloud Gateway]
    end
    
    subgraph "应用服务层"
        subgraph "Kubernetes集群"
            subgraph "Node 1"
                APP1[应用实例1<br/>Spring Boot]
                APP2[应用实例2<br/>Spring Boot]
            end
            subgraph "Node 2"
                APP3[应用实例3<br/>Spring Boot]
                APP4[应用实例4<br/>Spring Boot]
            end
            subgraph "Node 3"
                APP5[应用实例5<br/>Spring Boot]
                APP6[应用实例6<br/>Spring Boot]
            end
        end
    end
    
    subgraph "数据存储层"
        subgraph "MySQL集群"
            DB1[MySQL Master<br/>主数据库]
            DB2[MySQL Slave1<br/>从数据库]
            DB3[MySQL Slave2<br/>从数据库]
        end
        
        subgraph "Redis集群"
            R1[Redis Master1<br/>缓存节点]
            R2[Redis Master2<br/>缓存节点]
            R3[Redis Master3<br/>缓存节点]
            R4[Redis Slave1<br/>备份节点]
            R5[Redis Slave2<br/>备份节点]
            R6[Redis Slave3<br/>备份节点]
        end
        
        subgraph "Elasticsearch集群"
            ES1[ES Master1<br/>主节点]
            ES2[ES Master2<br/>主节点]
            ES3[ES Master3<br/>主节点]
            ES4[ES Data1<br/>数据节点]
            ES5[ES Data2<br/>数据节点]
        end
    end
    
    subgraph "监控日志层"
        subgraph "监控系统"
            PROM[Prometheus<br/>指标收集]
            GRAF[Grafana<br/>监控面板]
            ALERT[AlertManager<br/>告警管理]
        end
        
        subgraph "日志系统"
            ELK1[Elasticsearch<br/>日志存储]
            LOG[Logstash<br/>日志处理]
            KIB[Kibana<br/>日志分析]
        end
    end
    
    subgraph "外部服务"
        PAY1[支付宝API]
        PAY2[微信支付API]
        SMS[短信服务]
        OSS[对象存储]
    end
    
    %% 连接关系
    U1 --> CDN
    U2 --> CDN
    U3 --> CDN
    CDN --> LB1
    CDN --> LB2
    
    LB1 --> GW1
    LB1 --> GW2
    LB2 --> GW2
    LB2 --> GW3
    
    GW1 --> APP1
    GW1 --> APP3
    GW1 --> APP5
    GW2 --> APP2
    GW2 --> APP4
    GW2 --> APP6
    GW3 --> APP1
    GW3 --> APP3
    
    APP1 --> DB1
    APP2 --> DB1
    APP3 --> DB2
    APP4 --> DB2
    APP5 --> DB3
    APP6 --> DB3
    
    APP1 --> R1
    APP2 --> R2
    APP3 --> R3
    APP4 --> R1
    APP5 --> R2
    APP6 --> R3
    
    APP1 --> ES4
    APP2 --> ES4
    APP3 --> ES5
    APP4 --> ES5
    APP5 --> ES4
    APP6 --> ES5
    
    DB1 --> DB2
    DB1 --> DB3
    R1 --> R4
    R2 --> R5
    R3 --> R6
    
    APP1 --> PAY1
    APP2 --> PAY2
    APP3 --> SMS
    APP4 --> OSS
    
    APP1 --> PROM
    APP2 --> PROM
    APP3 --> PROM
    PROM --> GRAF
    PROM --> ALERT
    
    APP1 --> LOG
    APP2 --> LOG
    APP3 --> LOG
    LOG --> ELK1
    ELK1 --> KIB
```

## 环境部署方案

### 开发环境 (Development)

```mermaid
graph TB
    subgraph "开发环境"
        DEV1[开发者本地环境]
        DEV2[共享开发环境]
    end
    
    subgraph "本地服务"
        APP[Spring Boot应用]
        DB[MySQL 8.0]
        REDIS[Redis 7.4.0]
        ES[Elasticsearch]
    end
    
    subgraph "外部服务"
        MOCK[Mock服务<br/>支付/短信]
    end
    
    DEV1 --> APP
    DEV2 --> APP
    APP --> DB
    APP --> REDIS
    APP --> ES
    APP --> MOCK
```

**配置特点**：
- 单机部署，资源占用小
- 使用内嵌数据库或轻量级数据库
- Mock外部服务，避免真实调用
- 热部署支持，提高开发效率

**环境配置**：
```yaml
# application-dev.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/muying_mall_dev
    username: dev_user
    password: dev_password
  
  redis:
    host: localhost
    port: 6379
    database: 0
  
  elasticsearch:
    uris: http://localhost:9200

logging:
  level:
    com.muyingmall: DEBUG
```

### 测试环境 (Testing)

```mermaid
graph TB
    subgraph "测试环境"
        TEST[测试服务器]
    end
    
    subgraph "容器化部署"
        subgraph "Docker Compose"
            APP1[应用容器1]
            APP2[应用容器2]
            DB[MySQL容器]
            REDIS[Redis容器]
            ES[Elasticsearch容器]
            NGINX[Nginx容器]
        end
    end
    
    subgraph "外部服务"
        SANDBOX[沙箱环境<br/>支付/短信]
    end
    
    TEST --> NGINX
    NGINX --> APP1
    NGINX --> APP2
    APP1 --> DB
    APP2 --> DB
    APP1 --> REDIS
    APP2 --> REDIS
    APP1 --> ES
    APP2 --> ES
    APP1 --> SANDBOX
    APP2 --> SANDBOX
```

**配置特点**：
- Docker容器化部署
- 多实例负载均衡
- 使用沙箱环境测试外部服务
- 自动化测试集成

**Docker Compose配置**：
```yaml
version: '3.8'
services:
  app:
    image: muying-mall:latest
    ports:
      - "8080-8082:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=test
    depends_on:
      - mysql
      - redis
    deploy:
      replicas: 2
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: test_password
      MYSQL_DATABASE: muying_mall_test
    volumes:
      - mysql_data:/var/lib/mysql
  
  redis:
    image: redis:7.4.0-alpine
    volumes:
      - redis_data:/data
  
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
```

### 生产环境 (Production)

```mermaid
graph TB
    subgraph "生产环境架构"
        subgraph "多可用区部署"
            subgraph "可用区A"
                LB1[负载均衡器A]
                K8S1[Kubernetes集群A]
                DB1[MySQL主库]
                REDIS1[Redis集群A]
            end
            
            subgraph "可用区B"
                LB2[负载均衡器B]
                K8S2[Kubernetes集群B]
                DB2[MySQL从库1]
                REDIS2[Redis集群B]
            end
            
            subgraph "可用区C"
                LB3[负载均衡器C]
                K8S3[Kubernetes集群C]
                DB3[MySQL从库2]
                REDIS3[Redis集群C]
            end
        end
        
        subgraph "共享服务"
            ES[Elasticsearch集群]
            MONITOR[监控系统]
            LOG[日志系统]
        end
    end
    
    LB1 --> K8S1
    LB2 --> K8S2
    LB3 --> K8S3
    
    K8S1 --> DB1
    K8S2 --> DB2
    K8S3 --> DB3
    
    K8S1 --> REDIS1
    K8S2 --> REDIS2
    K8S3 --> REDIS3
    
    K8S1 --> ES
    K8S2 --> ES
    K8S3 --> ES
    
    DB1 --> DB2
    DB1 --> DB3
```

**配置特点**：
- 多可用区部署，高可用保障
- Kubernetes容器编排
- 数据库主从复制，读写分离
- Redis集群，分片存储
- 完整的监控和日志系统

## Kubernetes部署配置

### 应用部署配置

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: muying-mall-app
  namespace: production
spec:
  replicas: 6
  selector:
    matchLabels:
      app: muying-mall
  template:
    metadata:
      labels:
        app: muying-mall
    spec:
      containers:
      - name: app
        image: muying-mall:v1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: MYSQL_HOST
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: host
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

### 服务配置

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: muying-mall-service
  namespace: production
spec:
  selector:
    app: muying-mall
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: muying-mall-lb
  namespace: production
spec:
  selector:
    app: muying-mall
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 配置管理

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: production
data:
  application.yml: |
    server:
      port: 8080
    spring:
      datasource:
        url: jdbc:mysql://${MYSQL_HOST}:3306/muying_mall
        username: ${MYSQL_USER}
        password: ${MYSQL_PASSWORD}
      redis:
        cluster:
          nodes: ${REDIS_NODES}
      elasticsearch:
        uris: ${ES_URIS}

---
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
  namespace: production
type: Opaque
data:
  host: bXlzcWwtbWFzdGVyLnByb2R1Y3Rpb24uc3ZjLmNsdXN0ZXIubG9jYWw=
  username: cm9vdA==
  password: cGFzc3dvcmQ=
```

## 监控和日志架构

### 监控系统部署

```mermaid
graph TB
    subgraph "监控架构"
        subgraph "指标收集"
            PROM[Prometheus Server]
            NODE[Node Exporter]
            APP[Application Metrics]
            MYSQL[MySQL Exporter]
            REDIS[Redis Exporter]
        end
        
        subgraph "可视化"
            GRAF[Grafana]
            DASH1[应用监控面板]
            DASH2[基础设施面板]
            DASH3[业务指标面板]
        end
        
        subgraph "告警"
            ALERT[AlertManager]
            WEBHOOK[Webhook通知]
            EMAIL[邮件通知]
            SMS[短信通知]
        end
    end
    
    NODE --> PROM
    APP --> PROM
    MYSQL --> PROM
    REDIS --> PROM
    
    PROM --> GRAF
    GRAF --> DASH1
    GRAF --> DASH2
    GRAF --> DASH3
    
    PROM --> ALERT
    ALERT --> WEBHOOK
    ALERT --> EMAIL
    ALERT --> SMS
```

### 日志系统部署

```mermaid
graph TB
    subgraph "日志架构"
        subgraph "日志收集"
            APP1[应用日志]
            APP2[访问日志]
            APP3[错误日志]
            FILEBEAT[Filebeat]
        end
        
        subgraph "日志处理"
            LOGSTASH[Logstash]
            KAFKA[Kafka队列]
        end
        
        subgraph "日志存储"
            ES1[Elasticsearch]
            ES2[日志索引]
            ES3[归档存储]
        end
        
        subgraph "日志分析"
            KIBANA[Kibana]
            DASH[日志面板]
            ALERT[日志告警]
        end
    end
    
    APP1 --> FILEBEAT
    APP2 --> FILEBEAT
    APP3 --> FILEBEAT
    FILEBEAT --> KAFKA
    KAFKA --> LOGSTASH
    LOGSTASH --> ES1
    ES1 --> ES2
    ES2 --> ES3
    ES1 --> KIBANA
    KIBANA --> DASH
    KIBANA --> ALERT
```

## 安全架构

### 网络安全

```mermaid
graph TB
    subgraph "安全架构"
        subgraph "边界安全"
            WAF[Web应用防火墙]
            DDoS[DDoS防护]
            CDN[CDN安全]
        end
        
        subgraph "网络安全"
            VPC[私有网络]
            SG[安全组]
            ACL[网络ACL]
        end
        
        subgraph "应用安全"
            SSL[SSL/TLS加密]
            JWT[JWT认证]
            RBAC[权限控制]
        end
        
        subgraph "数据安全"
            ENCRYPT[数据加密]
            BACKUP[数据备份]
            AUDIT[审计日志]
        end
    end
    
    WAF --> VPC
    DDoS --> VPC
    CDN --> VPC
    VPC --> SG
    SG --> ACL
    ACL --> SSL
    SSL --> JWT
    JWT --> RBAC
    RBAC --> ENCRYPT
    ENCRYPT --> BACKUP
    BACKUP --> AUDIT
```

## 容灾和备份策略

### 数据备份

```yaml
备份策略:
  数据库备份:
    - 全量备份: 每日凌晨2点
    - 增量备份: 每4小时一次
    - 备份保留: 30天
    - 异地备份: 每周同步到异地
  
  Redis备份:
    - RDB快照: 每小时一次
    - AOF日志: 实时写入
    - 备份保留: 7天
  
  文件备份:
    - 应用文件: 版本控制
    - 配置文件: 配置中心
    - 日志文件: 7天滚动
```

### 容灾方案

```mermaid
graph TB
    subgraph "主站点"
        MAIN[主数据中心]
        APP1[应用集群]
        DB1[数据库集群]
        CACHE1[缓存集群]
    end
    
    subgraph "备站点"
        BACKUP[备数据中心]
        APP2[应用集群]
        DB2[数据库集群]
        CACHE2[缓存集群]
    end
    
    subgraph "容灾切换"
        DNS[DNS切换]
        LB[负载均衡切换]
        DATA[数据同步]
    end
    
    MAIN --> BACKUP
    DB1 --> DB2
    CACHE1 --> CACHE2
    
    DNS --> MAIN
    DNS --> BACKUP
    LB --> APP1
    LB --> APP2
    DATA --> DB1
    DATA --> DB2
```

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
