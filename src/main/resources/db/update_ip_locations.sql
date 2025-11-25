-- 更新现有登录记录的地理位置信息
-- 注意：此脚本仅用于开发环境测试，生产环境请谨慎使用

-- 1. 将 IPv6 本地回环地址转换为 IPv4
UPDATE admin_login_records 
SET ip_address = '127.0.0.1' 
WHERE ip_address IN ('0:0:0:0:0:0:0:1', '::1');

-- 2. 更新本地地址的地理位置
UPDATE admin_login_records 
SET location = '本地' 
WHERE ip_address = '127.0.0.1' AND (location IS NULL OR location = '未知');

-- 3. 更新内网地址的地理位置
UPDATE admin_login_records 
SET location = '内网' 
WHERE (
    ip_address LIKE '192.168.%' OR 
    ip_address LIKE '10.%' OR 
    ip_address LIKE '172.16.%' OR 
    ip_address LIKE '172.17.%' OR 
    ip_address LIKE '172.18.%' OR 
    ip_address LIKE '172.19.%' OR 
    ip_address LIKE '172.20.%' OR 
    ip_address LIKE '172.21.%' OR 
    ip_address LIKE '172.22.%' OR 
    ip_address LIKE '172.23.%' OR 
    ip_address LIKE '172.24.%' OR 
    ip_address LIKE '172.25.%' OR 
    ip_address LIKE '172.26.%' OR 
    ip_address LIKE '172.27.%' OR 
    ip_address LIKE '172.28.%' OR 
    ip_address LIKE '172.29.%' OR 
    ip_address LIKE '172.30.%' OR 
    ip_address LIKE '172.31.%'
) AND (location IS NULL OR location = '未知');

-- 4. 查看更新结果
SELECT 
    ip_address,
    location,
    COUNT(*) as count
FROM admin_login_records
GROUP BY ip_address, location
ORDER BY count DESC;
