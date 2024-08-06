package com.yc.web.servlet.admin.admutils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class QQMailUtil {
    public void send(String str, String qqMail) throws MessagingException {
        // 创建对象
        Properties pro = new Properties();
        // 设置主机
        pro.setProperty("mail.host", "smtp.qq.com");
        // 设置传输协议
        pro.setProperty("mail.transport.protocol", "smtp");
        // 允许邮箱授权认证
        pro.setProperty("mail.smtp.auth", "true");
        // 邮箱授权认证
        // 创建认证器对象
        Auth auth = new Auth();
        // 获取一个session
        Session session = Session.getInstance(pro, auth);
        // 获取连接
        Transport ts = session.getTransport();
        // 连接邮箱
        ts.connect("smtp.qq.com", "#");
        // 创建邮件
        MimeMessage mimeMessage = new MimeMessage(session);
        // 设置发件人
        mimeMessage.setFrom(new InternetAddress("#@qq.com"));
        // 设置收件人
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(qqMail));
        // 设置邮件主题
        mimeMessage.setSubject("测试");
        // 设置邮件内容
        mimeMessage.setContent(str, "text/html;charset=utf-8");
        // 发送邮件
        ts.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        // 关闭连接
        ts.close();
        System.out.println("qq邮箱发送成功");
    }
}
