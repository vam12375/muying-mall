# 母婴商城开发环境搭建指南

本文档详细描述了母婴商城项目的开发环境搭建过程，包括所需软件的安装和配置，项目的导入和运行，以及开发工具的推荐配置。

## 环境要求

### 基础环境

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | 推荐使用Oracle JDK或者Adoptium OpenJDK |
| Maven | 3.8+ | 项目构建和依赖管理工具 |
| Git | 最新版 | 版本控制工具 |
| IDE | - | 推荐IntelliJ IDEA 2023.2+或Eclipse 2023-09+ |
| Docker | 最新版 | 用于容器化部署和本地依赖服务启动 |
| Docker Compose | 最新版 | 用于管理多容器应用 |

### 依赖服务

| 服务 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 缓存、分布式锁等 |
| Elasticsearch | 8.11.0 | 全文搜索引擎 |
| MinIO | 最新版 | 对象存储服务，用于存储图片等 |

## 安装步骤

### 1. 安装JDK 21

#### Windows

1. 下载JDK 21安装包：[Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html)或[Adoptium](https://adoptium.net/)
2. 运行安装包，按照向导完成安装
3. 设置环境变量：
   - 新建系统变量`JAVA_HOME`，值为JDK安装目录，如`C:\Program Files\Java\jdk-21`
   - 编辑系统变量`Path`，添加`%JAVA_HOME%\bin`
4. 验证安装：打开命令提示符，运行`java -version`，确认版本正确

#### macOS

1. 使用Homebrew安装：
   ```bash
   brew tap homebrew/cask-versions
   brew install --cask temurin21
   ```
2. 或者下载安装包手动安装
3. 验证安装：打开终端，运行`java -version`

#### Linux (Ubuntu/Debian)

1. 安装JDK：
   ```bash
   sudo apt update
   sudo apt install openjdk-21-jdk
   ```
2. 验证安装：运行`java -version`

#### Linux (CentOS/RHEL)

1. 安装JDK：
   ```bash
   sudo dnf install java-21-openjdk-devel
   ```
2. 验证安装：运行`java -version`

### 2. 安装Maven

#### Windows

1. 下载[Apache Maven](https://maven.apache.org/download.cgi)
2. 解压到指定目录，如`C:\Program Files\Apache\maven`
3. 设置环境变量：
   - 新建系统变量`MAVEN_HOME`，值为Maven解压目录
   - 编辑系统变量`Path`，添加`%MAVEN_HOME%\bin`
4. 验证安装：打开命令提示符，运行`mvn -version`

#### macOS

1. 使用Homebrew安装：
   ```bash
   brew install maven
   ```
2. 验证安装：打开终端，运行`mvn -version`

#### Linux

1. 安装Maven：
   ```bash
   sudo apt update           # Ubuntu/Debian
   sudo apt install maven
   ```
   或
   ```bash
   sudo dnf install maven    # CentOS/RHEL
   ```
2. 验证安装：运行`mvn -version`

### 3. 安装Docker与Docker Compose

#### Windows

1. 下载并安装[Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
2. 安装完成后启动Docker Desktop
3. 验证安装：打开命令提示符，运行`docker --version`和`docker-compose --version`

#### macOS

1. 下载并安装[Docker Desktop for Mac](https://www.docker.com/products/docker-desktop)
2. 安装完成后启动Docker Desktop
3. 验证安装：打开终端，运行`docker --version`和`docker-compose --version`

#### Linux

1. 安装Docker：
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install docker.io
   sudo systemctl enable --now docker
   sudo usermod -aG docker $USER
   ```
   或
   ```bash
   # CentOS/RHEL
   sudo dnf install docker
   sudo systemctl enable --now docker
   sudo usermod -aG docker $USER
   ```
2. 安装Docker Compose：
   ```bash
   sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   ```
3. 验证安装：重新登录后运行`docker --version`和`docker-compose --version`

### 4. 安装IDE (IntelliJ IDEA)

1. 下载并安装[IntelliJ IDEA](https://www.jetbrains.com/idea/download/)（推荐Ultimate版本）
2. 安装以下插件：
   - Lombok
   - Spring Boot Assistant
   - MapStruct Support
   - SonarLint
   - Docker
   - Redis Client

## 项目设置

### 1. 克隆项目

```bash
git clone https://github.com/vam12375/muying-mall.git
cd muying-mall
```

### 2. 启动依赖服务

项目根目录包含Docker Compose配置文件，用于快速启动开发环境所需的依赖服务：

```bash
docker-compose -f docker-compose-dev.yml up -d
```

这会启动以下服务：
- MySQL数据库（端口3306）
- Redis缓存（端口6379）
- Elasticsearch（端口9200）
- MinIO对象存储（端口9000，控制台9001）

### 3. 初始化数据库

```bash
# 进入MySQL容器
docker exec -it muying-mall-mysql bash

# 连接MySQL
mysql -u root -p

# 输入密码（默认为root）后执行
CREATE DATABASE muying_mall CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'muying_user'@'%' IDENTIFIED BY 'muying_pass';
GRANT ALL PRIVILEGES ON muying_mall.* TO 'muying_user'@'%';
FLUSH PRIVILEGES;
EXIT;

# 退出容器
exit

# 导入初始数据
mysql -h 127.0.0.1 -P 3306 -u muying_user -p muying_mall < main.sql
```

### 4. 配置开发环境

1. 创建`application-dev.yml`文件：

```bash
cp src/main/resources/application.yml src/main/resources/application-dev.yml
```

2. 编辑`application-dev.yml`，配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/muying_mall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: muying_user
    password: muying_pass
  
  redis:
    host: localhost
    port: 6379
    
  elasticsearch:
    uris: http://localhost:9200
    
minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucket: muying-mall
```

3. 配置IDE:
   - 打开项目
   - 设置JDK版本为21
   - 设置Maven配置
   - 导入项目为Spring Boot项目

### 5. 构建项目

```bash
mvn clean package -DskipTests
```

### 6. 运行项目

#### 从IDE运行

1. 在IDE中找到`MuyingMallApplication.java`
2. 右键选择"Run"或"Debug"
3. 确保运行配置中的活动配置文件(Active Profile)设置为`dev`

#### 从命令行运行

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

运行成功后，可以访问：
- API文档：http://localhost:8080/api/swagger-ui.html
- 健康检查：http://localhost:8080/api/actuator/health

## 开发工具配置

### Maven配置

创建或编辑`~/.m2/settings.xml`：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Repository</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
  
  <profiles>
    <profile>
      <id>jdk-21</id>
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>21</jdk>
      </activation>
      <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <maven.compiler.compilerVersion>21</maven.compiler.compilerVersion>
      </properties>
    </profile>
  </profiles>
</settings>
```

### IntelliJ IDEA配置

1. **代码风格配置**：
   - 导入项目根目录下的`idea-code-style.xml`
   - 路径：Settings > Editor > Code Style > Java > Import Scheme

2. **Lombok插件**：
   - 确保已安装Lombok插件
   - 启用注解处理：Settings > Build, Execution, Deployment > Compiler > Annotation Processors > Enable annotation processing

3. **热部署配置**：
   - 启用自动构建：Settings > Build, Execution, Deployment > Compiler > Build project automatically
   - 启用运行时构建：Registry (Ctrl+Shift+Alt+/) > compiler.automake.allow.when.app.running

## 常见问题排查

### 数据库连接失败

- 检查MySQL容器是否正常运行：`docker ps | grep mysql`
- 检查数据库用户名和密码是否正确
- 确认防火墙是否允许3306端口访问

### Redis连接问题

- 验证Redis服务是否运行：`docker ps | grep redis`
- 尝试使用Redis客户端连接：`redis-cli -h localhost ping`

### Elasticsearch连接问题

- 检查服务状态：`curl -X GET "localhost:9200/_cluster/health"`
- 查看容器日志：`docker logs muying-mall-elasticsearch`

### Maven构建失败

- 检查Maven版本：`mvn -version`
- 清理Maven缓存：`mvn dependency:purge-local-repository`
- 确认Maven设置中是否配置了正确的镜像和代理

## 进一步配置

### 配置邮件服务

编辑`application-dev.yml`，添加：

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### 配置支付宝沙箱

编辑`application-dev.yml`，添加：

```yaml
alipay:
  appId: 你的沙箱应用ID
  privateKey: 你的应用私钥
  publicKey: 支付宝公钥
  gatewayUrl: https://openapi.alipaydev.com/gateway.do
  returnUrl: http://localhost:8080/api/v1/payments/alipay/return
  notifyUrl: http://localhost:8080/api/v1/payments/alipay/notify
```

### 配置微信支付沙箱

编辑`application-dev.yml`，添加：

```yaml
wxpay:
  appId: 你的沙箱AppID
  mchId: 你的沙箱商户号
  mchKey: 你的沙箱API密钥
  notifyUrl: http://localhost:8080/api/v1/payments/wxpay/notify
  keyPath: classpath:wxpay/apiclient_cert.p12
```

## 附录

### 有用的命令

```bash
# 查看所有运行的容器
docker ps

# 停止所有开发环境容器
docker-compose -f docker-compose-dev.yml down

# Maven清理并跳过测试构建
mvn clean package -DskipTests

# 运行特定测试
mvn test -Dtest=UserServiceTest

# 查看依赖树
mvn dependency:tree
```

### 参考文档

- [Spring Boot文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MyBatis-Plus文档](https://baomidou.com/guide/)
- [Redis官方文档](https://redis.io/documentation)
- [Elasticsearch文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Docker文档](https://docs.docker.com/) 