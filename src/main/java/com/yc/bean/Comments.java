package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments {
    private Integer id;//pinglid
    private Integer item_id;//商品id
    private String user_name;//用户名
    private Integer user_id;//用户id
    private String topic;//主题
    private String comment;//内容
    private Integer rating;//评分
    private String created_at;//时间
    private String image;
    private String item_name;
}
