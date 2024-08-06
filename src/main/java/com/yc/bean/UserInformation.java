package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInformation implements Serializable {
    private Integer id;
    private String  accountname;  // 账号
    private String  password;
    private String  name;
    private String  status;
    private String  email;
    private Integer logincount;
    private String creationtime;
    private String updatetime;
    private String role;
    private String mobile;
    private String image;

}
