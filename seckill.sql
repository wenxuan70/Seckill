SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for seckill
-- ----------------------------
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill`  (
  `seckill_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '秒杀活动名称',
  `number` int(11) NOT NULL COMMENT '库存数量',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '秒杀活动创建时间',
  `start_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '秒杀开始时间',
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '秒杀结束时间',
  PRIMARY KEY (`seckill_id`) USING BTREE,
  INDEX `idx_start_time`(`start_time`) USING BTREE,
  INDEX `idx_end_time`(`end_time`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1004 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of seckill
-- ----------------------------
INSERT INTO `seckill` VALUES (1000, '1元秒杀iphone', 100, '2020-08-23 20:21:08', '2020-08-26 00:00:00', '2020-08-27 00:00:00');
INSERT INTO `seckill` VALUES (1001, '1元秒杀Mac', 100, '2020-08-23 20:21:08', '2020-08-26 21:32:00', '2020-08-26 21:33:00');
INSERT INTO `seckill` VALUES (1002, '1元秒杀特斯拉', 100, '2020-08-23 20:21:08', '2020-08-27 00:00:00', '2020-08-28 00:00:00');
INSERT INTO `seckill` VALUES (1003, '1元秒杀旺仔牛奶10箱', 100, '2020-08-23 20:21:08', '2020-08-26 00:00:00', '2020-08-27 00:00:00');

-- ----------------------------
-- Table structure for success_killed
-- ----------------------------
DROP TABLE IF EXISTS `success_killed`;
CREATE TABLE `success_killed`  (
  `seckill_id` bigint(20) NOT NULL COMMENT '秒杀id',
  `user_phone` bigint(20) NOT NULL COMMENT '用户手机号',
  `state` tinyint(4) NOT NULL DEFAULT -1 COMMENT '状态表示:-1:无效,0:成功,1:已付款,2:已发货',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`seckill_id`, `user_phone`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秒杀成功明细表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of success_killed
-- ----------------------------
INSERT INTO `success_killed` VALUES (1002, 12312345678, 0, '2020-08-29 01:28:10');

-- ----------------------------
-- Procedure structure for execute_seckill
-- ----------------------------
DROP PROCEDURE IF EXISTS `execute_seckill`;
delimiter ;;
CREATE PROCEDURE `execute_seckill`(in v_seckill_id bigint, in v_phone bigint,
        in v_kill_time timestamp, out r_result int)
BEGIN
        DECLARE insert_count int DEFAULT 0;
        START TRANSACTION;
        insert ignore into `success_killed`(seckill_id,user_phone,state)
            value (v_seckill_id, v_phone, 0);
        select row_count() into insert_count;
        IF (insert_count  < 0) THEN
            ROLLBACK;
            set r_result = -2;
        ELSEIF (insert_count = 0) THEN
            ROLLBACK;
            set r_result = -1;
        ELSE
            update seckill
            set number = number - 1
            where seckill_id = v_seckill_id
            and start_time <= v_kill_time
            and end_time >= v_kill_time
            and number > 0;
            select row_count() into insert_count;
            IF (insert_count = 0) THEN
                ROLLBACK;
                set r_result = 0;
            ELSEIF (insert_count < 0) THEN
                ROLLBACK;
                set r_result = -2;
            ELSE
                COMMIT;
                set r_result = 1;
            END IF;
        END IF;
    END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
