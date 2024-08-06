package com.yc.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUser {
    private String  username;  // 账号
    private String  password;
    private String  role;    // 角色 1为商家   2为平台
    private String  creationtime;
    private String  phone;  // 电话
}
