package com.yc.web.servlet.admin.admutils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

// 认证器
public class Auth extends Authenticator {

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("#@qq.com", "#");
    }

}
