-- 添加连续签到奖励规则
INSERT INTO points_rule (title, description, type, points_value, sort_order, enabled, create_time, update_time) 
VALUES 
('连续签到3天奖励', '连续签到满3天额外奖励200积分', 'signin_continuous_3', 200, 8, 1, NOW(), NOW()),
('连续签到7天奖励', '连续签到满7天额外奖励500积分', 'signin_continuous_7', 500, 9, 1, NOW(), NOW()); 