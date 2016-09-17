/*
Navicat MySQL Data Transfer

Source Server         : Local
Source Server Version : 50523
Source Host           : localhost:3306
Source Database       : over

Target Server Type    : MYSQL
Target Server Version : 50523
File Encoding         : 65001

Date: 2016-05-03 02:42:32
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for character_acp_info
-- ----------------------------
DROP TABLE IF EXISTS `character_acp_info`;
CREATE TABLE `character_acp_info` (
  `obj_id` int(32) NOT NULL,
  `potion` int(32) NOT NULL,
  `min` int(16) DEFAULT NULL,
  `max` int(16) DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`obj_id`,`potion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
