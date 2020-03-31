/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.100.5
 Source Server Type    : MySQL
 Source Server Version : 50642
 Source Host           : 
 Source Schema         : ztuo_wallet

 Target Server Type    : MySQL
 Target Server Version : 50642
 File Encoding         : 65001

 Date: 30/03/2020 17:41:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;






-- ----------------------------
-- Table structure for t_address_btc
-- ----------------------------
DROP TABLE IF EXISTS `t_address_btc`;
CREATE TABLE `t_address_btc` (
  `address` varchar(100) NOT NULL COMMENT '地址',
  `script_address` varchar(2000) NOT NULL COMMENT '隔离见证地址',
  `pri_key` varchar(2000) NOT NULL COMMENT '私钥',
  `aes_key` varchar(2000) NOT NULL COMMENT 'aes密钥',
  `password` varchar(1000) DEFAULT NULL COMMENT '生成geth密钥的密码',
  `sys_id` varchar(20) DEFAULT NULL COMMENT '系统ID(app/mgr/app-mgr)',
  `is_enable` varchar(1) DEFAULT '1' COMMENT '是否启用1:启用',
  `is_delete` varchar(1) DEFAULT '0' COMMENT '是否删除1:删除',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(400) DEFAULT NULL COMMENT '备注',
  `master_flg` varchar(1) NOT NULL DEFAULT '0' COMMENT '大钱包标识 0：否 1:是',
  `user_id` varchar(20) DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`address`) USING BTREE,
  UNIQUE KEY `acount_userId` (`user_id`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='BTC账户列表';



-- ----------------------------
-- Table structure for t_address_eth
-- ----------------------------
DROP TABLE IF EXISTS `t_address_eth`;
CREATE TABLE `t_address_eth` (
  `address` varchar(100) NOT NULL COMMENT '地址',
  `pri_key` varchar(2000) NOT NULL COMMENT '私钥',
  `aes_key` varchar(2000) NOT NULL COMMENT 'aes密钥',
  `password` varchar(1000) DEFAULT NULL COMMENT '生成geth密钥的密码',
  `sys_id` varchar(20) DEFAULT NULL COMMENT '系统ID',
  `is_enable` varchar(1) DEFAULT NULL COMMENT '是否启用',
  `is_delete` varchar(1) DEFAULT NULL COMMENT '是否删除',
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(400) DEFAULT NULL COMMENT '备注',
  `master_flg` varchar(1) NOT NULL DEFAULT '0' COMMENT '大钱包标识 0：否 1:是',
  `nonce` int(16) DEFAULT '0' COMMENT '交易次数',
  `user_id` varchar(20) DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`address`) USING BTREE,
  UNIQUE KEY `acount_userId` (`user_id`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='账户';

) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='BTC账户列表';

-- ----------------------------
-- Table structure for t_address_usdt_omni
-- ----------------------------
DROP TABLE IF EXISTS `t_address_usdt_omni`;
CREATE TABLE `t_address_usdt_omni` (
  `address` varchar(100) NOT NULL COMMENT '地址',
  `script_address` varchar(2000) NOT NULL COMMENT '隔离见证地址',
  `pri_key` varchar(2000) NOT NULL COMMENT '私钥',
  `aes_key` varchar(2000) NOT NULL COMMENT 'aes密钥',
  `password` varchar(1000) DEFAULT NULL COMMENT '生成geth密钥的密码',
  `sys_id` varchar(20) DEFAULT NULL COMMENT '系统ID(app/mgr/app-mgr)',
  `is_enable` varchar(1) DEFAULT '1' COMMENT '是否启用1:启用',
  `is_delete` varchar(1) DEFAULT '0' COMMENT '是否删除1:删除',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(400) DEFAULT NULL COMMENT '备注',
  `master_flg` varchar(1) NOT NULL DEFAULT '0' COMMENT '大钱包标识 0：否 1:是',
  `user_id` varchar(20) DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`address`) USING BTREE,
  UNIQUE KEY `acount_userId` (`user_id`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='BTC账户列表';

) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='BTC余额表';

-- ----------------------------
-- Table structure for t_balance_btc
-- ----------------------------
DROP TABLE IF EXISTS `t_balance_btc`;
CREATE TABLE `t_balance_btc` (
  `address` varchar(100) NOT NULL COMMENT '账户id',
  `currency` varchar(10) NOT NULL COMMENT '币种',
  `amount` decimal(24,10) NOT NULL COMMENT '余额',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address`,`currency`) USING BTREE,
  KEY `btc_address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='BTC余额表';

) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='以太坊余额表';

-- ----------------------------
-- Table structure for t_balance_eth
-- ----------------------------
DROP TABLE IF EXISTS `t_balance_eth`;
CREATE TABLE `t_balance_eth` (
  `address` varchar(100) NOT NULL COMMENT '账户id',
  `currency` varchar(10) NOT NULL COMMENT '币种',
  `amount` decimal(40,10) NOT NULL COMMENT '余额',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address`,`currency`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='以太坊余额表';

) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='BTC余额表';

-- ----------------------------
-- Table structure for t_balance_usdt_omni
-- ----------------------------
DROP TABLE IF EXISTS `t_balance_usdt_omni`;
CREATE TABLE `t_balance_usdt_omni` (
  `address` varchar(100) NOT NULL COMMENT '账户id',
  `currency` varchar(10) NOT NULL COMMENT '币种',
  `amount` decimal(24,10) NOT NULL COMMENT '余额',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address`,`currency`) USING BTREE,
  KEY `btc_address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='BTC余额表';

-- ----------------------------
-- Table structure for t_block_record_btc
-- ----------------------------
DROP TABLE IF EXISTS `t_block_record_btc`;
CREATE TABLE `t_block_record_btc` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `txid` varchar(255) NOT NULL COMMENT '交易哈希id',
  `from_address` varchar(255) DEFAULT NULL COMMENT '发起者地址',
  `to_address` varchar(255) DEFAULT NULL COMMENT '接收者地址',
  `amount` decimal(24,10) NOT NULL COMMENT '交易金额',
  `block_height` varchar(10) DEFAULT NULL COMMENT '交易区块高度',
  `block_hash` varchar(100) DEFAULT NULL COMMENT '交易HASHID',
  `trade_fee` decimal(24,10) DEFAULT NULL COMMENT '交易手续费（btc/kb）',
  `status` char(2) NOT NULL COMMENT '交易状态',
  `currency` char(20) DEFAULT NULL COMMENT '币种',
  `sys_id` varchar(32) NOT NULL COMMENT '所属系统ID',
  `is_callback` char(1) DEFAULT NULL COMMENT '是否回调',
  `is_delete` char(1) DEFAULT NULL COMMENT '是否删除',
  `pay_type` varchar(2) DEFAULT NULL COMMENT '交易类型',
  `order_no` varchar(100) DEFAULT NULL COMMENT '本系统流水号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ak_uq_tx_hashid` (`txid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2204 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='区块交易记录';

-- ----------------------------
-- Table structure for t_block_record_eth
-- ----------------------------
DROP TABLE IF EXISTS `t_block_record_eth`;
CREATE TABLE `t_block_record_eth` (
  `txid` varchar(255) NOT NULL COMMENT '交易哈希id',
  `true_from_address` varchar(255) NOT NULL COMMENT '发起者真实地址',
  `from_address` varchar(255) NOT NULL COMMENT '发起者地址',
  `to_address` varchar(255) NOT NULL COMMENT '接收者地址',
  `amount` decimal(24,10) NOT NULL COMMENT '交易金额',
  `trade_block_no` varchar(10) DEFAULT NULL COMMENT '交易区块序号',
  `block_hash` varchar(100) DEFAULT NULL COMMENT '交易HASHID',
  `trade_fee` decimal(24,10) DEFAULT NULL COMMENT '交易手续费',
  `status` char(2) NOT NULL COMMENT '交易状态',
  `input_data` text COMMENT '前置数据',
  `currency` char(5) DEFAULT NULL COMMENT '币种',
  `nonce_str` varchar(10) DEFAULT NULL COMMENT '随机数',
  `gas_limit` decimal(24,10) DEFAULT NULL COMMENT 'GAS上限',
  `gas_used` decimal(24,10) DEFAULT NULL COMMENT 'GAS消耗量',
  `gas_price` decimal(24,10) DEFAULT NULL COMMENT 'GAS单价',
  `sys_id` varchar(32) NOT NULL COMMENT '所属系统ID',
  `is_callback` char(1) DEFAULT NULL COMMENT '是否回调',
  `is_delete` char(1) DEFAULT NULL COMMENT '是否删除',
  `pay_type` varchar(2) DEFAULT NULL COMMENT '交易类型',
  `order_no` varchar(100) DEFAULT NULL COMMENT '本系统流水号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`txid`) USING BTREE,
  UNIQUE KEY `ak_uq_tx_hashid` (`txid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='区块交易记录';


SET FOREIGN_KEY_CHECKS = 1;
