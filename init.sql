/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80023
 Source Host           : localhost:3306
 Source Schema         : mixlive

 Target Server Type    : MySQL
 Target Server Version : 80023
 File Encoding         : 65001

 Date: 10/04/2023 21:50:03
*/

# 创建名为mixlive的数据库
CREATE DATABASE IF NOT EXISTS mixlive;

# 使用mixlive数据库
USE mixlive;

-- ----------------------------
-- Table structure for active_users
-- ----------------------------
DROP TABLE IF EXISTS `active_users`;
CREATE TABLE `active_users`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `date` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '统计时间',
  `login_user_num` int(0) NOT NULL COMMENT '活跃用户数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for area_info
-- ----------------------------
DROP TABLE IF EXISTS `area_info`;
CREATE TABLE `area_info`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台',
  `area_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区类型(主机, 网游)',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区类型名(主机, 网游)',
  `area_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区id',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区名',
  `area_pic` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分区图url',
  `short_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分区简称',
  `index_area` int(0) NOT NULL COMMENT '映射后的分区名字',
  `index_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '映射后的分区类型',
  `created` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `modified` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4018 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for area_info_index
-- ----------------------------
DROP TABLE IF EXISTS `area_info_index`;
CREATE TABLE `area_info_index`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `type_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区类型名',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区名',
  `area_pic` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区图片',
  `priority` bigint(0) NOT NULL COMMENT '优先级(数值越小，优先级越高，0为没有优先级)',
  `created` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `modefied` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3359 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台分区映射后的分区' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for area_type_index
-- ----------------------------
DROP TABLE IF EXISTS `area_type_index`;
CREATE TABLE `area_type_index`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `area_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `area_type_platform` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (1, '网游', 'bilibili', '网游');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (2, '手游', 'bilibili', '手游');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (3, '单机', 'bilibili', '单机游戏');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (4, '娱乐', 'bilibili', '娱乐');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (5, '娱乐', 'bilibili', '电台');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (6, '娱乐', 'bilibili', '虚拟主播');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (7, '其他', 'bilibili', '生活');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (8, '其他', 'bilibili', '知识');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (9, '网游', 'bilibili', '赛事');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (10, '其他', 'bilibili', '购物');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (11, '其他', 'douyu', '星势力');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (12, '网游', 'douyu', '网游竞技');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (13, '手游', 'douyu', '手游休闲');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (14, '单机', 'douyu', '单机热游');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (15, '娱乐', 'douyu', '娱乐天地');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (16, '娱乐', 'douyu', '颜值');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (17, '其他', 'douyu', '科技文化');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (18, '其他', 'douyu', '正能量');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (19, '其他', 'douyu', '语音直播');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (20, '其他', 'douyu', '京斗云');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (21, '其他', 'douyu', '语音互动');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (22, '其他', 'douyu', '赛车竞技');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (23, '网游', 'huya', '网游');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (24, '单机', 'huya', '单机');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (25, '手游', 'huya', '手游');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (26, '娱乐', 'huya', '娱乐');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (27, '网游', 'cc', '网游');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (28, '手游', 'cc', '手游');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (29, '网游', 'cc', '网游竞技');
INSERT INTO `area_type_index`(`id`, `area_type`, `platform`, `area_type_platform`) VALUES (30, '娱乐', 'cc', '娱乐');

-- ----------------------------
-- Table structure for follows
-- ----------------------------
DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows`  (
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户uid',
  `roomId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户关注房间号',
  `platform` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关注房间所在平台',
  `followDate` datetime(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '关注事件（暂没用到）',
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 530404 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_mail
-- ----------------------------
DROP TABLE IF EXISTS `user_mail`;
CREATE TABLE `user_mail`  (
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'uid',
  `mail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱地址',
  PRIMARY KEY (`uid`, `mail`) USING BTREE,
  UNIQUE INDEX `index1`(`mail`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for userinfo
-- ----------------------------
DROP TABLE IF EXISTS `userinfo`;
CREATE TABLE `userinfo`  (
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户uid',
  `userName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `passWord` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `head` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像（暂未使用）',
  `isActived` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '是否开启屏蔽',
  `allContent` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所有屏蔽词',
  `selectedContent` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生效屏蔽词',
  `douyuLevel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '斗鱼屏蔽等级',
  `bilibiliLevel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT 'bilibili屏蔽等级',
  `huyaLevel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '虎牙屏蔽等级',
  `ccLevel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT 'cc屏蔽等级',
  `egameLevel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '企鹅直播屏蔽等级',
  `created` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `modified` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `last_login` timestamp(0) NULL DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
