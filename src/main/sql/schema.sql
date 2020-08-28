-- 创建数据库
create database seckill default charset = utf8mb4;
-- 使用数据库
use seckill;
-- 创建秒杀库存表
CREATE TABLE seckill(
`seckill_id` bigint not null auto_increment comment '商品库存id',
`name` varchar(20) not null comment '商品名称',
`number` int not null comment '库存数量',
`create_time` timestamp not null default current_timestamp comment '创建时间',
`start_time` timestamp not null comment '开始时间',
`end_time` timestamp not null comment '结束时间',
primary key (seckill_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)ENGINE=Innodb auto_increment = 1000 default charset = utf8mb4 comment '秒杀库存表';

-- 初始化数据
insert into
    seckill(name, number, start_time, end_time)
value
    ('999元秒杀iphoneXs', 100 , '2020-8-23 00:00:00', '2020-8-23 00:00:00'),
    ('1元秒杀小米手环4', 100 , '2020-8-23 00:00:00', '2020-8-23 00:00:00'),
    ('1元秒杀特斯拉', 100 , '2020-8-23 00:00:00', '2020-8-23 00:00:00'),
    ('100元秒杀小米10至尊版', 100 , '2020-8-23 00:00:00', '2020-8-23 00:00:00');

-- 秒杀成功明细表
create table success_killed(
`seckill_id` bigint not null comment '秒杀id',
`user_phone` bigint not null comment '用户手机号',
`state` tinyint not null default -1 comment '状态表示:-1:无效,0:成功,1:已付款,2:已发货',
`create_time` timestamp not null comment '创建时间',
primary key (seckill_id,user_phone), /*联合主键*/
key idx_create_time(create_time)
)ENGINE=Innodb auto_increment = 1000 default charset = utf8mb4 comment '秒杀成功明细表';
