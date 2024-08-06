package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private Integer id; // 优惠券的唯一标识符，主键，自增
    private String code; // 优惠券码，必须唯一
    private Double discount; // 折扣金额
    private String expiration_date; // 优惠券的过期日期
    private String usage_limit; // 优惠券的使用限制
    private Integer used_count; // 已使用次数
    private String status; // 优惠券的状态（例如：active、expired）
    private String type; // 优惠券类型（例如：personal 或 general）
    private String created_at; // 优惠券创建时间
    private String updated_at; // 优惠券更新时间
}
