package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    private int id;
    private int user_id;
    private int coupon_id;
    private Integer used;   //0未使用 1已使用 2已过期
}