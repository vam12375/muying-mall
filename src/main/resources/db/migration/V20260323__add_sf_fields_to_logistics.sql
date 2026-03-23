-- ============================================================
-- 顺丰沙箱接入：logistics 表新增第三方物流字段
-- 关联设计文档: docs/plans/2026-03-23-sf-express-phase1-design.md
-- 执行前请确保已在 logistics_company 表中维护顺丰等第三方供应商记录
-- 执行方式: 手动执行（项目当前未启用 Flyway/Liquibase）
-- ============================================================

-- --------------------------------------------------------
-- Step 1: 新增第三方物流相关字段
-- --------------------------------------------------------
ALTER TABLE logistics
  ADD COLUMN provider_code         VARCHAR(32)  DEFAULT NULL COMMENT '物流供应商代码，如 SF' AFTER `remark`,
  ADD COLUMN provider_type         VARCHAR(32)  DEFAULT NULL COMMENT 'THIRD_PARTY / LOCAL_SIMULATION' AFTER `provider_code`,
  ADD COLUMN provider_waybill_no  VARCHAR(64)  DEFAULT NULL COMMENT '第三方真实运单号' AFTER `provider_type`,
  ADD COLUMN provider_status      VARCHAR(32)  DEFAULT NULL COMMENT '第三方标准化状态' AFTER `provider_waybill_no`,
  ADD COLUMN route_sync_mode      VARCHAR(16)  DEFAULT NULL COMMENT 'POLLING / PUSH' AFTER `provider_status`,
  ADD COLUMN route_sync_status   VARCHAR(16)  DEFAULT NULL COMMENT 'PENDING / SYNCING / SUCCESS / FAILED' AFTER `route_sync_mode`,
  ADD COLUMN last_route_sync_time DATETIME     DEFAULT NULL COMMENT '最近路由同步时间' AFTER `route_sync_status`;

-- --------------------------------------------------------
-- Step 2: 添加索引（按供应商类型/路由同步状态查询时用到）
-- --------------------------------------------------------
ALTER TABLE logistics
  ADD INDEX idx_provider_type       (provider_type),
  ADD INDEX idx_route_sync_status   (route_sync_status);
