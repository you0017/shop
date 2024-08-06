package com.yc.bean;

import lombok.Data;

@Data
public class AdminReturnOrder {
    private Integer return_id; // 退货订单的唯一标识符
    private String tracking_number; // 退货单号、售后单号
    private String order_id; // 订单号
    private String product_name; // 产品名称
    private Double refund_amount; // 退款金额
    private String name; // 用户名
    private String return_date; // 申请时间
    private String return_status; // 状态 1、待处理  2、已完成
    private String back_type; // 类型  退款类型 1：仅退款 2：退款退货
    private String return_reason; // 原因
    private Integer return_quantity; // 退货产品的数量
    private String tracking_company;//快递公司
    private Integer product_id; // 退货产品的唯一标识符
}
