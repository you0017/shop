package com.yc.web.servlet.admin.bean;

import com.yc.bean.Item;
import lombok.Data;

@Data
public class Logistics {
    private Integer order_id; // '订单id' ,
    private String shipping_date; // '发货时间'
    private Integer shipping_status; // '物流状态  1：已发货 2：已签收'
    private String receiver_name; // '收货人'
    private String receiver_phone; // '电话'
    private String receiver_address; //'地址'
    private String logistics_company; // '物流公司'
    private String tracking_number; // '快递单号'
    private String remarks; // '备注'
}
