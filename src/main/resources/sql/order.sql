CREATE TABLE `tb_order` (
      `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
      `order_code` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单编码',
      `status` smallint(3) DEFAULT NULL COMMENT '订单状态',
      `name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单名称',
      `price` decimal(12,2) DEFAULT NULL COMMENT '价格',
      `delete_flag` tinyint(2) NOT NULL DEFAULT 0 COMMENT '删除标记，0未删除  1已删除',
      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
      `create_user_code` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建人',
      `update_user_code` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新人',
      `version` int(11) NOT NULL DEFAULT 0 COMMENT '版本号',
      `remark` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='订单表';