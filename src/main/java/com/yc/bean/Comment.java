package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Integer id;//pinglid
    private Integer item_id;//商品id
    private String user_name;//用户名
    private Integer user_id;//用户id
    private String topic;//主题
    private String comment;//内容
    private Integer rating;//评分
    private String created_at;//时间
    private String image;
    private String likes;   //点赞
    private String dislikes;//踩
    private String parent_comment_id;   //回复的某个评论
    private Integer shop_reply; // 过审状态
    private String shop_backcomment; // 商家回复内容
    private String shop_backcomment_status; // 商家回复状态  0 未回复  1 已回复
    private String shop_backcomment_time; // 商家回复时间
}
