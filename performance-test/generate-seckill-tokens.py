#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
秒杀压测Token生成脚本
生成10,000个测试用户的Token数据用于JMeter压测
"""

import csv
import hashlib
import base64

def generate_mock_token(user_id, username):
    """
    生成模拟JWT Token
    注意：这是测试数据，实际使用时需要替换为真实的Token生成逻辑
    """
    # 模拟JWT Header
    header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"

    # 模拟JWT Payload (Base64编码)
    payload_str = f'{{"userId":{user_id},"username":"{username}","iat":1700000000,"exp":1731536000}}'
    payload = base64.b64encode(payload_str.encode()).decode().rstrip('=')

    # 模拟签名
    signature_base = f"{user_id}_{username}_secret_key"
    signature = hashlib.sha256(signature_base.encode()).hexdigest()[:43]

    return f"{header}.{payload}.{signature}"

def generate_seckill_tokens(output_file, num_users=10000):
    """
    生成秒杀压测用户Token CSV文件

    Args:
        output_file: 输出CSV文件路径
        num_users: 生成的用户数量
    """
    print(f"开始生成 {num_users} 个秒杀测试用户Token...")

    with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)

        # 写入表头
        writer.writerow(['userId', 'username', 'token'])

        # 生成用户数据
        for i in range(1, num_users + 1):
            user_id = i
            username = f"seckill_user_{i:04d}"
            token = generate_mock_token(user_id, username)

            writer.writerow([user_id, username, token])

            # 每1000条打印进度
            if i % 1000 == 0:
                print(f"已生成 {i}/{num_users} 条记录...")

    print(f"✓ Token文件生成完成: {output_file}")
    print(f"✓ 总计生成 {num_users} 条测试用户数据")

if __name__ == "__main__":
    output_path = "data/seckill-tokens.csv"
    generate_seckill_tokens(output_path, 10000)
