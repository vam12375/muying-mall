-- 育儿知识分类表
CREATE TABLE IF NOT EXISTS `parenting_tip_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿知识分类表';

-- 育儿知识表
CREATE TABLE IF NOT EXISTS `parenting_tip` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识ID',
  `category_id` int NOT NULL COMMENT '分类ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `content` text COMMENT '内容',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图片',
  `author` varchar(50) DEFAULT '母婴商城' COMMENT '作者',
  `view_count` int DEFAULT 0 COMMENT '浏览量',
  `like_count` int DEFAULT 0 COMMENT '点赞数',
  `comment_count` int DEFAULT 0 COMMENT '评论数',
  `is_hot` tinyint DEFAULT 0 COMMENT '是否热门：0-否，1-是',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `publish_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿知识表';

-- 育儿知识评论表
CREATE TABLE IF NOT EXISTS `parenting_tip_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `tip_id` bigint NOT NULL COMMENT '知识ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tip_id` (`tip_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿知识评论表';


-- 初始化分类数据
INSERT INTO `parenting_tip_category` (`name`, `icon`, `sort`, `status`) VALUES
('孕期护理', '🤰', 1, 1),
('新生儿护理', '👶', 2, 1),
('婴儿喂养', '🍼', 3, 1),
('宝宝健康', '💪', 4, 1),
('早教启蒙', '📚', 5, 1),
('亲子互动', '👨‍👩‍👧', 6, 1);

-- 初始化知识数据
INSERT INTO `parenting_tip` (`category_id`, `title`, `summary`, `content`, `author`, `view_count`, `is_hot`) VALUES
(1, '孕期营养补充指南', '孕期是宝宝发育的关键时期，合理的营养补充对母婴健康至关重要。', '孕期营养补充需要注意以下几点：\n\n1. 叶酸：孕早期每天补充400-800微克叶酸，预防神经管缺陷。\n\n2. 铁元素：孕中晚期需要增加铁的摄入，预防贫血。\n\n3. 钙质：每天需要1000-1200毫克钙，保证胎儿骨骼发育。\n\n4. DHA：促进胎儿大脑和视力发育。\n\n5. 蛋白质：每天增加15-25克优质蛋白。', '母婴专家', 1256, 1),
(2, '新生儿脐带护理', '正确的脐带护理可以预防感染，促进愈合。', '脐带护理要点：\n\n1. 保持干燥：每天用75%酒精消毒脐带根部。\n\n2. 避免摩擦：尿布不要盖住脐带。\n\n3. 观察异常：如有红肿、渗液、异味要及时就医。\n\n4. 自然脱落：一般7-14天自然脱落，不要人为拉扯。', '儿科医生', 2341, 1),
(3, '母乳喂养技巧', '正确的母乳喂养姿势和技巧，让哺乳更轻松。', '母乳喂养要点：\n\n1. 正确含乳：宝宝嘴巴张大，含住大部分乳晕。\n\n2. 喂养姿势：摇篮式、橄榄球式、侧卧式。\n\n3. 按需喂养：新生儿每2-3小时喂一次。\n\n4. 判断吃饱：每天6-8次小便，体重正常增长。', '母婴护理师', 3456, 1),
(4, '宝宝发烧护理', '宝宝发烧时的正确护理方法。', '发烧护理要点：\n\n1. 测量体温：腋下温度超过37.5℃为发烧。\n\n2. 物理降温：温水擦浴、多喝水、减少衣物。\n\n3. 药物退烧：体温超过38.5℃可使用退烧药。\n\n4. 就医指征：3个月以下婴儿发烧、高烧不退、精神萎靡、抽搐。', '儿科医生', 4123, 1),
(5, '0-1岁早教游戏', '适合0-1岁宝宝的早教游戏和玩具推荐。', '早教游戏推荐：\n\n0-3个月：黑白卡片、追视训练、抚触按摩\n\n4-6个月：摇铃玩具、镜子游戏、翻身练习\n\n7-9个月：躲猫猫、积木、爬行训练\n\n10-12个月：套圈玩具、音乐玩具、学步车', '早教专家', 2345, 1),
(6, '亲子游戏推荐', '增进亲子感情的互动游戏。', '亲子游戏推荐：\n\n室内游戏：搭积木、角色扮演、手工制作、亲子阅读\n\n户外游戏：公园散步、沙滩玩耍、骑车、放风筝\n\n游戏原则：全身心投入、跟随宝宝兴趣、适当引导、享受过程', '亲子教育专家', 1678, 1);
