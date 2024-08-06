package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private String id;
    private String name;    //商品名
    private Double price;   //价格
    private Integer stock;  //货量
    private String image;   //图片地址
    private String category;    //种类
    private String brand;   //品牌
    private String spec;    //规格
    private int sold;   //销量
    private int comment_count;  //评论数
    private String create_time;
    private String update_time;
    private Integer rating; //评分
    private Integer status; //是否下架 1?0
    private Integer warning_value; //预警值
    private Integer warning_value_status; //预警类型 1?0
    private String stock_up;  // 库存上限
    private String stock_down; //库存下限
    private List<ItemPic> itempic;//副图
    private String item_details;  //商品详情|描述

}
