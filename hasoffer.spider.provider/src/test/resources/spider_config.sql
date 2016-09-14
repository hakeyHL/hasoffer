/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50630
Source Host           : localhost:3306
Source Database       : bigdata

Target Server Type    : MYSQL
Target Server Version : 50630
File Encoding         : 65001

Date: 2016-09-06 15:17:26
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `spider_config`
-- ----------------------------
DROP TABLE IF EXISTS `spider_config`;
CREATE TABLE `spider_config` (
  `id` bigint(20) NOT NULL,
  `website` varchar(255) NOT NULL,
  `thread_num` int(11) NOT NULL,
  `time_out` int(11) NOT NULL,
  `retry_times` int(11) NOT NULL,
  `interval_times` int(11) NOT NULL,
  `task_level` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of spider_config
-- ----------------------------
INSERT INTO `spider_config` VALUES ('1', 'ALIEXPRESS', '1', '1', '1', '1', 'LEVEL_1');
