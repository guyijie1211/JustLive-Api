SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for areaFollow
-- ----------------------------
DROP TABLE IF EXISTS `areaFollow`;
CREATE TABLE `areaFollow` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
  `areaType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区类型',
  `area` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区',
  `index` int DEFAULT NULL COMMENT '关注顺序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for follows
-- ----------------------------
DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows` (
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户uid',
  `roomId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户关注房间号',
  `platform` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关注房间所在平台',
  `followDate` datetime(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '关注事件（暂没用到）',
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=144106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for system_dark
-- ----------------------------
DROP TABLE IF EXISTS `system_dark`;
CREATE TABLE `system_dark` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `system` varchar(255) NOT NULL COMMENT '系统名',
  `dark_code` int NOT NULL COMMENT '深色模式编号',
  `light_code` int NOT NULL COMMENT '浅色模式编号',
  PRIMARY KEY (`id`,`system`,`dark_code`,`light_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for userinfo
-- ----------------------------
DROP TABLE IF EXISTS `userinfo`;
CREATE TABLE `userinfo` (
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户uid',
  `userName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `passWord` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `head` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像（暂未使用）',
  `isActived` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '是否开启屏蔽',
  `allContent` varchar(255) DEFAULT NULL COMMENT '所有屏蔽词',
  `selectedContent` varchar(255) DEFAULT NULL COMMENT '生效屏蔽词',
  `douyuLevel` varchar(255) NOT NULL DEFAULT '1' COMMENT '斗鱼屏蔽等级',
  `bilibiliLevel` varchar(255) NOT NULL DEFAULT '1' COMMENT 'bilibili屏蔽等级',
  `huyaLevel` varchar(255) NOT NULL DEFAULT '1' COMMENT '虎牙屏蔽等级',
  `ccLevel` varchar(255) NOT NULL DEFAULT '1' COMMENT 'cc屏蔽等级',
  `egameLevel` varchar(255) NOT NULL DEFAULT '1' COMMENT '企鹅直播屏蔽等级',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
