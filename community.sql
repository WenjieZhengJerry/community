/*
Navicat MySQL Data Transfer

Source Server         : 云数据库RDS
Source Server Version : 50720
Source Host           : rm-wz9yeq4nmj6100210eo.mysql.rds.aliyuncs.com:3306
Source Database       : community

Target Server Type    : MYSQL
Target Server Version : 50720
File Encoding         : 65001

Date: 2019-08-20 17:09:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(19) NOT NULL,
  `type` int(10) NOT NULL,
  `commentator` bigint(19) NOT NULL,
  `gmt_create` bigint(19) NOT NULL,
  `gmt_modified` bigint(19) NOT NULL,
  `like_count` bigint(19) DEFAULT '0',
  `content` text,
  `comment_count` int(10) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `notifier` bigint(19) NOT NULL,
  `receiver` bigint(19) NOT NULL,
  `outer_id` bigint(19) NOT NULL,
  `type` int(10) NOT NULL,
  `gmt_create` bigint(19) NOT NULL,
  `status` int(10) NOT NULL DEFAULT '0',
  `notifier_name` varchar(100) DEFAULT NULL,
  `outer_title` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(50) NOT NULL,
  `description` text NOT NULL,
  `gmt_create` bigint(19) NOT NULL,
  `gmt_modified` bigint(19) NOT NULL,
  `creator` bigint(19) NOT NULL,
  `comment_count` int(10) DEFAULT '0',
  `view_count` int(10) DEFAULT '0',
  `like_count` int(10) DEFAULT '0',
  `tag` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` varchar(100) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `token` char(36) NOT NULL,
  `gmt_create` bigint(19) NOT NULL,
  `gmt_modified` bigint(19) NOT NULL,
  `bio` varchar(256) DEFAULT NULL,
  `avatar_url` varchar(100) DEFAULT NULL,
  `email` varchar(25) DEFAULT NULL,
  `password` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
