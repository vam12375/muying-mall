# 母婴商城开发环境搭建指南

## 概述

本文档详细介绍了母婴商城系统开发环境的搭建步骤，包括必需的软件安装、配置说明和常见问题解决方案。

## 系统要求

### 硬件要求
- **CPU**: 4核心以上
- **内存**: 8GB以上（推荐16GB）
- **硬盘**: 50GB以上可用空间
- **网络**: 稳定的互联网连接

### 操作系统支持
- Windows 10/11
- macOS 10.15+
- Ubuntu 18.04+
- CentOS 7+

## 必需软件安装

### 1. Java开发环境

#### 安装Java 21
```bash
# Windows (使用Chocolatey)
choco install openjdk21

# macOS (使用Homebrew)
brew install openjdk@21

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# CentOS/RHEL
sudo yum install java-21-openjdk-devel
```

#### 验证安装
```bash
java -version
javac -version
```

#### 配置环境变量
```bash
# Windows
set JAVA_HOME=C:\Program Files\OpenJDK\openjdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

# Linux/macOS
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### 2. Maven构建工具

#### 安装Maven 3.8+
```bash
# Windows (使用Chocolatey)
choco install maven

# macOS (使用Homebrew)
brew install maven

# Ubuntu/Debian
sudo apt install maven

# 手动安装
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz
sudo mv apache-maven-3.9.6 /opt/maven
```

#### 配置Maven
```bash
# 配置环境变量
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH

# 验证安装
mvn -version
```

#### Maven配置文件 (settings.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <localRepository>${user.home}/.m2/repository</localRepository>
  
  <mirrors>
    <mirror>
      <id>aliyun-maven</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/central</url>
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

### 3. 数据库环境

#### 安装MySQL 8.0
```bash
# Windows
# 下载MySQL Installer from https://dev.mysql.com/downloads/installer/

# macOS
brew install mysql@8.0
brew services start mysql@8.0

# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server-8.0

# CentOS/RHEL
sudo yum install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

#### MySQL配置
```sql
-- 创建数据库
CREATE DATABASE muying_mall CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 创建用户
CREATE USER 'muying_user'@'localhost' IDENTIFIED BY 'muying_password';
GRANT ALL PRIVILEGES ON muying_mall.* TO 'muying_user'@'localhost';
FLUSH PRIVILEGES;

-- 导入数据库结构
mysql -u muying_user -p muying_mall < main.sql
```

#### 安装Redis 7.4.0
```bash
# Windows
# 下载Redis for Windows from https://github.com/microsoftarchive/redis/releases

# macOS
brew install redis
brew services start redis

# Ubuntu/Debian
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server

# CentOS/RHEL
sudo yum install epel-release
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis
```

#### Redis配置
```bash
# 编辑Redis配置文件
sudo vim /etc/redis/redis.conf

# 主要配置项
bind 127.0.0.1
port 6379
requirepass your_redis_password
maxmemory 1gb
maxmemory-policy allkeys-lru
```

#### 安装Elasticsearch 8.11
```bash
# 下载并安装Elasticsearch
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-9.2.1-linux-x86_64.tar.gz
tar -xzf elasticsearch-9.2.1-linux-x86_64.tar.gz
sudo mv elasticsearch-9.2.1 /opt/elasticsearch

# 创建elasticsearch用户
sudo useradd elasticsearch
sudo chown -R elasticsearch:elasticsearch /opt/elasticsearch

# 启动Elasticsearch
sudo -u elasticsearch /opt/elasticsearch/bin/elasticsearch
```

### 4. 开发工具

#### 推荐IDE
- **IntelliJ IDEA Ultimate** (推荐)
- **Eclipse IDE for Enterprise Java Developers**
- **Visual Studio Code** (轻量级选择)

#### IntelliJ IDEA配置
```bash
# 安装必需插件
- Lombok Plugin
- Spring Boot Plugin
- MyBatis Plugin
- Redis Plugin
- Database Navigator

# JVM配置 (idea64.exe.vmoptions)
-Xms2048m
-Xmx4096m
-XX:ReservedCodeCacheSize=1024m
-XX:+UseConcMarkSweepGC
-XX:SoftRefLRUPolicyMSPerMB=50
```

## 项目配置

### 1. 克隆项目
```bash
git clone https://github.com/your-org/muying-mall.git
cd muying-mall
```

### 2. 配置文件设置

#### 创建私有配置文件
```bash
# 复制配置模板
cp src/main/resources/application-private.yml.example src/main/resources/application-private.yml
```

#### 编辑私有配置 (application-private.yml)
```yaml
spring:
  datasource:
    password: your_mysql_password
  
  data:
    redis:
      password: your_redis_password
  
  elasticsearch:
    username: elastic
    password: your_elasticsearch_password

# 支付宝配置
alipay:
  app-id: your_alipay_app_id
  private-key: your_alipay_private_key
  public-key: your_alipay_public_key

# 微信支付配置
wechat:
  pay:
    app-id: your_wechat_app_id
    mch-id: your_wechat_mch_id
    mch-serial-number: your_wechat_serial_number
    api-v3-key: your_wechat_api_v3_key

# JWT配置
jwt:
  secret: your_jwt_secret_key_at_least_256_bits_long
```

### 3. 依赖安装
```bash
# 安装项目依赖
mvn clean install

# 跳过测试安装
mvn clean install -DskipTests
```

### 4. 数据库初始化
```bash
# 执行数据库脚本
mysql -u muying_user -p muying_mall < main.sql

# 或者使用IDE的数据库工具执行SQL文件
```

## 启动项目

### 1. 启动基础服务
```bash
# 启动MySQL
sudo systemctl start mysql

# 启动Redis
sudo systemctl start redis

# 启动Elasticsearch
sudo systemctl start elasticsearch
```

### 2. 启动Spring Boot应用
```bash
# 命令行启动
mvn spring-boot:run

# 或者指定配置文件
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# IDE中启动
# 运行 MuyingMallApplication.java 主类
```

### 3. 验证启动
```bash
# 检查应用健康状态
curl http://localhost:8080/api/actuator/health

# 访问API文档
open http://localhost:8080/api/swagger-ui.html

# 测试API接口
curl http://localhost:8080/api/test/connection
```

## 开发工具配置

### 1. Git配置
```bash
# 配置用户信息
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 配置编辑器
git config --global core.editor "vim"

# 配置换行符
git config --global core.autocrlf input  # Linux/macOS
git config --global core.autocrlf true   # Windows
```

### 2. 代码格式化
```bash
# 安装Google Java Format插件
# IntelliJ IDEA: File -> Settings -> Plugins -> Google Java Format

# 配置代码风格
# File -> Settings -> Editor -> Code Style -> Java
# 导入 google-java-format.xml
```

### 3. 数据库工具
```bash
# 推荐数据库管理工具
- DBeaver (免费)
- Navicat (商业)
- DataGrip (JetBrains)
- MySQL Workbench (官方)
```

## 常见问题解决

### 1. Java版本问题
```bash
# 问题：Java版本不匹配
# 解决：确保使用Java 21
java -version
# 如果版本不对，重新安装或配置JAVA_HOME
```

### 2. Maven依赖下载失败
```bash
# 问题：依赖下载超时或失败
# 解决：配置国内镜像源
# 编辑 ~/.m2/settings.xml，添加阿里云镜像
```

### 3. 数据库连接失败
```bash
# 问题：数据库连接被拒绝
# 解决：检查数据库服务状态和配置
sudo systemctl status mysql
mysql -u root -p
```

### 4. Redis连接失败
```bash
# 问题：Redis连接超时
# 解决：检查Redis服务和配置
sudo systemctl status redis
redis-cli ping
```

### 5. 端口占用问题
```bash
# 问题：8080端口被占用
# 解决：查找并终止占用进程
lsof -i :8080
kill -9 <PID>

# 或者修改应用端口
# 在application.yml中修改server.port
```

### 6. 内存不足
```bash
# 问题：编译或运行时内存不足
# 解决：增加JVM内存配置
export MAVEN_OPTS="-Xmx2048m"

# IDE中增加运行内存
# Run Configuration -> VM Options: -Xmx2048m
```

## 开发环境验证清单

- [ ] Java 21安装并配置正确
- [ ] Maven 3.8+安装并配置正确
- [ ] MySQL 8.0安装并运行正常
- [ ] Redis 7.4.0安装并运行正常
- [ ] Elasticsearch 8.11安装并运行正常
- [ ] IDE安装并配置插件
- [ ] 项目代码克隆成功
- [ ] 配置文件设置完成
- [ ] 依赖安装成功
- [ ] 数据库初始化完成
- [ ] 应用启动成功
- [ ] API接口测试通过
- [ ] Swagger文档可访问

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
