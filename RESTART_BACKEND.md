# 重启后端服务

## 问题
`ProductSkuController` 新添加的Controller需要重启后端才能生效。

## 解决方案

### Windows
```bash
# 停止当前运行的后端
# 按 Ctrl + C 停止

# 重新启动
cd muying-mall
mvn clean spring-boot:run
```

### 或者使用IDE
1. 停止当前运行的应用
2. 重新运行 `MuyingMallApplication`

## 验证
启动成功后，访问：
```
http://localhost:8080/api/products/120/skus
```

应该返回SKU数据而不是404错误。
