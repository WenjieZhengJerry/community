/*
Navicat MySQL Data Transfer

Source Server         : lngfun
Source Server Version : 50727
Source Host           : 129.204.15.163:3306
Source Database       : community

Target Server Type    : MYSQL
Target Server Version : 50727
File Encoding         : 65001

Date: 2019-11-12 16:11:34
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for collection
-- ----------------------------
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(19) NOT NULL,
  `question_id` bigint(19) NOT NULL,
  `gmt_create` bigint(19) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;

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
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(19) NOT NULL COMMENT '被关注者',
  `follower_id` bigint(19) NOT NULL COMMENT '粉丝',
  `gmt_create` bigint(19) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for like
-- ----------------------------
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(19) NOT NULL,
  `user_id` bigint(19) NOT NULL,
  `type` int(10) NOT NULL COMMENT '给问题点赞类型为1，给评论点赞类型为2',
  `gmt_create` bigint(19) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8;

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
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8;

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
  `collection_count` int(10) DEFAULT '0',
  `tag` varchar(256) DEFAULT NULL,
  `category_type` int(10) DEFAULT NULL COMMENT '分类',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `openid` char(32) DEFAULT NULL,
  `account_id` varchar(100) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `token` char(36) NOT NULL,
  `gmt_create` bigint(19) NOT NULL,
  `gmt_modified` bigint(19) NOT NULL,
  `bio` varchar(256) DEFAULT NULL,
  `avatar_url` varchar(256) DEFAULT NULL,
  `email` varchar(25) DEFAULT NULL,
  `password` varchar(36) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `blog` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;
