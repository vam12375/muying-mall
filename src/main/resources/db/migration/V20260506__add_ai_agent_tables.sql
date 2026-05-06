-- ============================================================
-- AI Agent 业务流程自动化平台：会话、消息、工具调用日志与人工工单表
-- 执行方式：项目当前未强制启用 Flyway，可按需手动导入 MySQL。
-- 设计目标：保留电商核心单体一致性，仅为 FastAPI Agent 增加最小闭环数据。
-- ============================================================

CREATE TABLE IF NOT EXISTS ai_conversation (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI 会话ID',
  conversation_no VARCHAR(64) NOT NULL COMMENT '会话编号，便于前后端展示和排查',
  user_id INT DEFAULT NULL COMMENT '用户ID，允许游客演示场景为空',
  channel VARCHAR(32) NOT NULL DEFAULT 'WEB' COMMENT '来源渠道：WEB/ADMIN/API',
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN' COMMENT '会话状态：OPEN/CLOSED/HANDOFF',
  current_intent VARCHAR(64) DEFAULT NULL COMMENT '最近一次识别的意图',
  risk_level VARCHAR(32) NOT NULL DEFAULT 'LOW' COMMENT '风险等级：LOW/MEDIUM/HIGH',
  last_message VARCHAR(500) DEFAULT NULL COMMENT '最近一条用户消息摘要',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_conversation_no (conversation_no),
  KEY idx_ai_conversation_user (user_id),
  KEY idx_ai_conversation_status (status),
  KEY idx_ai_conversation_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent 会话表';

CREATE TABLE IF NOT EXISTS ai_message (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI 消息ID',
  conversation_id BIGINT NOT NULL COMMENT '会话ID',
  user_id INT DEFAULT NULL COMMENT '用户ID',
  role VARCHAR(32) NOT NULL COMMENT '消息角色：USER/ASSISTANT/TOOL/SYSTEM',
  content TEXT NOT NULL COMMENT '消息内容',
  intent VARCHAR(64) DEFAULT NULL COMMENT '消息对应意图',
  risk_level VARCHAR(32) NOT NULL DEFAULT 'LOW' COMMENT '风险等级',
  tool_call_log_id BIGINT DEFAULT NULL COMMENT '关联工具调用日志ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_ai_message_conversation (conversation_id),
  KEY idx_ai_message_user (user_id),
  KEY idx_ai_message_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent 消息表';

CREATE TABLE IF NOT EXISTS ai_tool_call_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '工具调用日志ID',
  trace_id VARCHAR(64) NOT NULL COMMENT '单次 Agent 执行链路ID',
  conversation_id BIGINT DEFAULT NULL COMMENT '会话ID',
  message_id BIGINT DEFAULT NULL COMMENT '触发工具调用的消息ID',
  user_id INT DEFAULT NULL COMMENT '用户ID',
  intent VARCHAR(64) DEFAULT NULL COMMENT '本次调用所属意图',
  risk_level VARCHAR(32) NOT NULL DEFAULT 'LOW' COMMENT '风险等级',
  tool_name VARCHAR(128) NOT NULL COMMENT '工具名称',
  tool_type VARCHAR(32) NOT NULL DEFAULT 'BUSINESS_API' COMMENT '工具类型：BUSINESS_API/RAG/LLM',
  request_payload JSON DEFAULT NULL COMMENT '请求参数快照',
  response_payload JSON DEFAULT NULL COMMENT '响应结果快照',
  success TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否调用成功',
  error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
  duration_ms BIGINT DEFAULT NULL COMMENT '耗时毫秒',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_ai_tool_trace (trace_id),
  KEY idx_ai_tool_conversation (conversation_id),
  KEY idx_ai_tool_user (user_id),
  KEY idx_ai_tool_name (tool_name),
  KEY idx_ai_tool_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent 工具调用日志表';

CREATE TABLE IF NOT EXISTS ai_support_ticket (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI 工单ID',
  ticket_no VARCHAR(64) NOT NULL COMMENT '工单编号',
  conversation_id BIGINT DEFAULT NULL COMMENT '来源会话ID',
  user_id INT DEFAULT NULL COMMENT '用户ID',
  order_id INT DEFAULT NULL COMMENT '关联订单ID',
  product_id INT DEFAULT NULL COMMENT '关联商品ID',
  title VARCHAR(200) NOT NULL COMMENT '工单标题',
  content TEXT NOT NULL COMMENT '工单内容',
  intent VARCHAR(64) DEFAULT NULL COMMENT '触发工单的意图',
  risk_level VARCHAR(32) NOT NULL DEFAULT 'MEDIUM' COMMENT '风险等级',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '工单状态：PENDING/PROCESSING/RESOLVED/CLOSED',
  source VARCHAR(32) NOT NULL DEFAULT 'AI_AGENT' COMMENT '来源：AI_AGENT/ADMIN',
  assignee_id INT DEFAULT NULL COMMENT '处理人ID',
  assignee_name VARCHAR(100) DEFAULT NULL COMMENT '处理人名称',
  handle_remark VARCHAR(1000) DEFAULT NULL COMMENT '处理备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  close_time DATETIME DEFAULT NULL COMMENT '关闭时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_ticket_no (ticket_no),
  KEY idx_ai_ticket_user (user_id),
  KEY idx_ai_ticket_status (status),
  KEY idx_ai_ticket_risk (risk_level),
  KEY idx_ai_ticket_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent 人工接管工单表';
