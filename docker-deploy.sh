#!/bin/bash

# 母婴商城后端 Docker 一键部署脚本
# 适用于 Linux/Mac 系统

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    print_info "检查Docker环境..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    print_success "Docker环境检查通过"
}

# 检查环境变量文件
check_env_file() {
    print_info "检查环境变量配置..."
    if [ ! -f .env ]; then
        print_warning ".env文件不存在，从.env.example创建"
        cp .env.example .env
        print_warning "请编辑.env文件，配置数据库密码、JWT密钥等信息"
        read -p "是否现在编辑.env文件？(y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            ${EDITOR:-vi} .env
        fi
    fi
    print_success "环境变量配置检查完成"
}

# 检查SQL初始化文件
check_sql_file() {
    print_info "检查数据库初始化文件..."
    if [ ! -f muying_mall.sql ]; then
        print_error "muying_mall.sql文件不存在，请确保数据库初始化脚本存在"
        exit 1
    fi
    print_success "数据库初始化文件检查通过"
}

# 停止并清理旧容器
cleanup() {
    print_info "停止并清理旧容器..."
    docker-compose down -v 2>/dev/null || docker compose down -v 2>/dev/null || true
    print_success "清理完成"
}

# 构建镜像
build_image() {
    print_info "开始构建Docker镜像..."
    docker-compose build --no-cache || docker compose build --no-cache
    print_success "镜像构建完成"
}

# 启动服务
start_services() {
    print_info "启动服务..."
    docker-compose up -d || docker compose up -d
    print_success "服务启动完成"
}

# 等待服务就绪
wait_for_services() {
    print_info "等待服务就绪..."
    
    # 等待MySQL
    print_info "等待MySQL启动..."
    for i in {1..30}; do
        if docker exec muying-mysql mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD:-muying123456} --silent &> /dev/null; then
            print_success "MySQL已就绪"
            break
        fi
        if [ $i -eq 30 ]; then
            print_error "MySQL启动超时"
            exit 1
        fi
        sleep 2
    done
    
    # 等待Redis
    print_info "等待Redis启动..."
    for i in {1..15}; do
        if docker exec muying-redis redis-cli ping &> /dev/null; then
            print_success "Redis已就绪"
            break
        fi
        if [ $i -eq 15 ]; then
            print_error "Redis启动超时"
            exit 1
        fi
        sleep 2
    done
    
    # 等待RabbitMQ
    print_info "等待RabbitMQ启动..."
    for i in {1..30}; do
        if docker exec muying-rabbitmq rabbitmq-diagnostics ping &> /dev/null; then
            print_success "RabbitMQ已就绪"
            break
        fi
        if [ $i -eq 30 ]; then
            print_error "RabbitMQ启动超时"
            exit 1
        fi
        sleep 2
    done
    
    # 等待后端应用
    print_info "等待后端应用启动（可能需要1-2分钟）..."
    for i in {1..60}; do
        if curl -f http://localhost:8080/api/actuator/health &> /dev/null; then
            print_success "后端应用已就绪"
            break
        fi
        if [ $i -eq 60 ]; then
            print_error "后端应用启动超时，请查看日志: docker logs muying-backend"
            exit 1
        fi
        sleep 3
    done
}

# 显示服务信息
show_info() {
    echo ""
    echo "=========================================="
    print_success "🎉 母婴商城后端部署成功！"
    echo "=========================================="
    echo ""
    echo "📋 服务信息："
    echo "  后端API:        http://localhost:8080/api"
    echo "  API文档:        http://localhost:8080/api/doc.html"
    echo "  健康检查:       http://localhost:8080/api/actuator/health"
    echo "  RabbitMQ管理:   http://localhost:15672 (guest/guest)"
    echo ""
    echo "🔧 常用命令："
    echo "  查看日志:       docker logs -f muying-backend"
    echo "  停止服务:       docker-compose down"
    echo "  重启服务:       docker-compose restart"
    echo "  查看状态:       docker-compose ps"
    echo ""
    echo "=========================================="
}

# 主函数
main() {
    echo ""
    echo "=========================================="
    echo "  母婴商城后端 Docker 一键部署"
    echo "=========================================="
    echo ""
    
    check_docker
    check_env_file
    check_sql_file
    
    read -p "是否清理旧容器和数据？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cleanup
    fi
    
    build_image
    start_services
    wait_for_services
    show_info
}

# 执行主函数
main
