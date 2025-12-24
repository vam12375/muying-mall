@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 母婴商城后端 Docker 一键部署脚本
REM 适用于 Windows 系统

echo.
echo ==========================================
echo   母婴商城后端 Docker 一键部署
echo ==========================================
echo.

REM 检查Docker是否安装
echo [INFO] 检查Docker环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker未安装，请先安装Docker Desktop
    pause
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Docker Compose未安装
        pause
        exit /b 1
    )
)
echo [SUCCESS] Docker环境检查通过
echo.

REM 检查环境变量文件
echo [INFO] 检查环境变量配置...
if not exist .env (
    echo [WARNING] .env文件不存在，从.env.example创建
    copy .env.example .env >nul
    echo [WARNING] 请编辑.env文件，配置数据库密码、JWT密钥等信息
    set /p EDIT_ENV="是否现在编辑.env文件？(y/n): "
    if /i "!EDIT_ENV!"=="y" (
        notepad .env
    )
)
echo [SUCCESS] 环境变量配置检查完成
echo.

REM 检查SQL初始化文件
echo [INFO] 检查数据库初始化文件...
if not exist muying_mall.sql (
    echo [ERROR] muying_mall.sql文件不存在
    pause
    exit /b 1
)
echo [SUCCESS] 数据库初始化文件检查通过
echo.

REM 询问是否清理旧容器
set /p CLEANUP="是否清理旧容器和数据？(y/n): "
if /i "!CLEANUP!"=="y" (
    echo [INFO] 停止并清理旧容器...
    docker-compose down -v 2>nul
    if errorlevel 1 (
        docker compose down -v 2>nul
    )
    echo [SUCCESS] 清理完成
    echo.
)

REM 构建镜像
echo [INFO] 开始构建Docker镜像...
docker-compose build --no-cache
if errorlevel 1 (
    docker compose build --no-cache
    if errorlevel 1 (
        echo [ERROR] 镜像构建失败
        pause
        exit /b 1
    )
)
echo [SUCCESS] 镜像构建完成
echo.

REM 启动服务
echo [INFO] 启动服务...
docker-compose up -d
if errorlevel 1 (
    docker compose up -d
    if errorlevel 1 (
        echo [ERROR] 服务启动失败
        pause
        exit /b 1
    )
)
echo [SUCCESS] 服务启动完成
echo.

REM 等待服务就绪
echo [INFO] 等待服务就绪...
echo [INFO] 等待MySQL启动...
timeout /t 10 /nobreak >nul

echo [INFO] 等待Redis启动...
timeout /t 5 /nobreak >nul

echo [INFO] 等待RabbitMQ启动...
timeout /t 10 /nobreak >nul

echo [INFO] 等待后端应用启动（可能需要1-2分钟）...
timeout /t 30 /nobreak >nul

REM 显示服务信息
echo.
echo ==========================================
echo [SUCCESS] 🎉 母婴商城后端部署成功！
echo ==========================================
echo.
echo 📋 服务信息：
echo   后端API:        http://localhost:8080/api
echo   API文档:        http://localhost:8080/api/doc.html
echo   健康检查:       http://localhost:8080/api/actuator/health
echo   RabbitMQ管理:   http://localhost:15672 (guest/guest)
echo.
echo 🔧 常用命令：
echo   查看日志:       docker logs -f muying-backend
echo   停止服务:       docker-compose down
echo   重启服务:       docker-compose restart
echo   查看状态:       docker-compose ps
echo.
echo ==========================================
echo.

pause
