package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    private Integer id; //
    private Integer order_id;   //订单id
    private Integer item_id;    //商品id
    private Integer num;    //数量
    private String name;   //名字
    private String spec;   //规格
    private Integer price;  //价格
    private Double actual_payment; //实际价格
    private String image;  //图片
    private String create_time;
    private String update_time;
    private Integer return_status; //退货状态 1 未退 2待处理 3已退
}
