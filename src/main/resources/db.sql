SHOW TABLES;

ALTER TABLE `order` DROP COLUMN comment_time;
ALTER TABLE item DROP COLUMN updater;
ALTER TABLE item DROP COLUMN isad;
ALTER TABLE item DROP COLUMN status;

ALTER TABLE `order`
    ADD COLUMN mobile varchar(100) NOT NULL;
ALTER TABLE `order` AUTO_INCREMENT = 10;
SELECT CONVERT(total_fee, DOUBLE) AS total_fee_float
FROM `order`;
SELECT CAST(total_fee AS FLOAT) AS total_fee_float
FROM `order`;
ALTER TABLE `order`
    ADD COLUMN address VARCHAR(255);
ALTER TABLE `order`
    MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;



ALTER TABLE item
    MODIFY COLUMN price DOUBLE(10,2);

select * from item limit 3 offset 0;

select count(*) from item;

ALTER TABLE item ADD status INT DEFAULT 0;



create table item_pic
(
    id       int primary key auto_increment, #主键，图片ID
    itemid        int          not null,          #外键，绑定商品ID
    image        varchar(500) not null         #图片或视频地址（OSS）
);

select * from item where id in (?,?,?) order by id asc;

select a.id,a.total_fee,a.status,a.create_time,a.address,b.num,b.name from `order` a,order_detail b where a.user_id=2 and a.id=b.order_id;

select * from `order` where status>0 limit 20 offset 0;

ALTER TABLE userinformation ADD COLUMN mobile VARCHAR(15);

-- 评论表
CREATE TABLE comments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          item_id INT NOT NULL,
                          user_id INT NOT NULL,
                          topic VARCHAR(255),
                          comment TEXT,
                          rating FLOAT DEFAULT 0,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE comments
    MODIFY COLUMN rating INT DEFAULT 0;
SELECT DISTINCT brand FROM item;

ALTER TABLE comments
    ADD COLUMN likes INT DEFAULT 0,
    ADD COLUMN dislikes INT DEFAULT 0,
    ADD COLUMN parent_comment_id INT DEFAULT 0;
ALTER TABLE comments
    DROP COLUMN parent_comment_id;

ALTER TABLE comments
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

select count(*)        from comments where item_id=? and parent_comment_id = 0

-- 判断是否购买过才能评论
select * from `order` left join order_detail on `order`.id=order_detail.item_id where order_detail.item_id=7 and `order`.user_id=15

select * from `order` left join order_detail on `order`.id=order_detail.order_id where `order`.id=39 and `order`.user_id=15 and order_detail.item_id=115



CREATE TABLE returnorders (
                              return_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '退货订单的唯一标识符，主键，自增。',
                              order_id INT NOT NULL COMMENT '原始订单的唯一标识符。',
                              customer_id INT NOT NULL COMMENT '退货客户的唯一标识符。',
                              product_id INT NOT NULL COMMENT '退货产品的唯一标识符。',
                              return_reason VARCHAR(255) NOT NULL COMMENT '退货原因的描述。',
                              return_quantity INT NOT NULL COMMENT '退货产品的数量。',
                              return_status VARCHAR(50) NOT NULL COMMENT '退货状态（例如：待处理、处理中、已完成）。',
                              return_date DATETIME NOT NULL COMMENT '退货申请日期。',
                              refund_amount DECIMAL(10, 2) NOT NULL COMMENT '退款金额。',
                              processing_date DATETIME COMMENT '退货处理日期。'
) COMMENT='退货订单表';

ALTER TABLE order_detail
    ADD COLUMN return_status TINYINT NOT NULL DEFAULT 1 COMMENT '退货状态（1: 未退货, 2: 已退货）';


ALTER TABLE returnorders
    ADD COLUMN tracking_number VARCHAR(50) COMMENT '快递单号';


update order_detail set return_status=1

select * from userinformation where 1=1  limit 5 offset 1

ALTER TABLE returnorders
    MODIFY refund_amount DOUBLE;

select name from (select * from item where (category like '%B%' or name like '%B%' or brand like '%B%' or spec like '%B%') and status=1 order by sold asc) a limit 6 offset 0


ALTER TABLE returnorders ALTER COLUMN tracking_company DROP DEFAULT;



CREATE TABLE coupons (
                         id INT AUTO_INCREMENT PRIMARY KEY,                 -- 优惠券的唯一标识符，主键，自增
                         code VARCHAR(50) NOT NULL UNIQUE,                  -- 优惠券码，必须唯一
                         discount DECIMAL(10, 2) NOT NULL,                  -- 折扣金额
                         expiration_date DATE NOT NULL,                     -- 优惠券的过期日期
                         usage_limit INT NOT NULL DEFAULT 1,                -- 优惠券的使用限制
                         used_count INT NOT NULL DEFAULT 0,                 -- 已使用次数
                         status VARCHAR(20) NOT NULL DEFAULT 'active',      -- 优惠券的状态（例如：active、expired）
                         type VARCHAR(20) NOT NULL,                         -- 优惠券类型（例如：personal 或 general）
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 优惠券创建时间
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 优惠券更新时间
);

ALTER TABLE coupons MODIFY usage_limit VARCHAR(50);

ALTER TABLE user_coupon
    MODIFY used INT NOT NULL DEFAULT 0;


select * from coupons where status='active'


CREATE TABLE user_coupon (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                coupon_id INT NOT NULL,
                used BOOLEAN NOT NULL DEFAULT FALSE  -- 是否使用过优惠券，默认为未使用
);


select * from coupons left join user_coupon on coupons.id = user_coupon.coupon_id where status='active' and user_id=11


ALTER TABLE `order_detail`
    ADD COLUMN actual_payment double NOT NULL DEFAULT 0 COMMENT '实际支付价格';

UPDATE `order_detail`
SET actual_payment = price;

ALTER TABLE `order`
    ADD COLUMN shipping_fee DECIMAL(10, 2) default 0;

ALTER TABLE `order`
    DROP COLUMN shipping_fee;

select distinct brand from item where category like '%手机%'
