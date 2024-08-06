package com.yc.web.servlet.admin;

import com.yc.utils.Sendmail;
import com.yc.web.servlet.admin.admutils.SMSUtil;

import javax.mail.Session;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;


public class test {
    public static void main(String[] args) {
        SMSUtil smsUtil = new SMSUtil();
        String random = smsUtil.random();
        System.out.println(random);


        LocalDateTime currentTime = LocalDateTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = currentTime.format(formatter);
        System.out.println("格式化后的时间为： " + formattedTime);

    }
}
