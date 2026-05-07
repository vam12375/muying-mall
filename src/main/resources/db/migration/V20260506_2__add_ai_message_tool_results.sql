-- 为 AI 助手历史回放保存结构化工具结果，支持商品推荐卡片等富内容恢复。
ALTER TABLE ai_message
  ADD COLUMN tool_results JSON DEFAULT NULL COMMENT 'Agent结构化工具结果，用于历史消息富内容回放'
  AFTER tool_call_log_id;
