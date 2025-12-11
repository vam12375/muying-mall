@echo off
chcp 65001 >nul
echo ========================================
echo   母婴商城压力测试启动脚本
echo ========================================
echo.

:: 设置JMeter路径（请根据实际安装路径修改）
set JMETER_HOME=E:\apache-jmeter-5.6.3

:: 检查JMeter是否存在
if not exist "%JMETER_HOME%\bin\jmeter.bat" (
    echo [错误] 未找到JMeter，请修改JMETER_HOME变量
    echo 当前路径: %JMETER_HOME%
    pause
    exit /b 1
)

:: 生成时间戳（避免空格问题）
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "TIMESTAMP=%dt:~0,8%_%dt:~8,4%"

:: 设置测试参数
set TEST_FILE=muying-mall-stress-test.jmx
set RESULT_FILE=stress-test-results_%TIMESTAMP%.jtl
set REPORT_DIR=report_%TIMESTAMP%

:: 清理旧报告目录
if exist "%REPORT_DIR%" rmdir /s /q "%REPORT_DIR%"

echo [信息] 测试文件: %TEST_FILE%
echo [信息] 结果文件: %RESULT_FILE%
echo [信息] 报告目录: %REPORT_DIR%
echo.

:: 运行压力测试
echo [开始] 执行压力测试...
"%JMETER_HOME%\bin\jmeter" -n -t "%TEST_FILE%" -l "%RESULT_FILE%" -e -o "%REPORT_DIR%" -q jmeter-report-template.properties

echo.
echo [完成] 测试执行完毕
echo [报告] 请打开 %REPORT_DIR%\index.html 查看测试报告
echo.
pause
