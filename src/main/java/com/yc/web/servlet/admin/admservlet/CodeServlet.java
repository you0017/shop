package com.yc.web.servlet.admin.admservlet;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * 枋心: 1. 生成一个数字，存到session
 *      (2). 干扰线，干扰点, 颜色，字体.
 *      3. 以图片形式返回客户端.      resp.setContentType("image/png");
 */
@WebServlet("/code.action")
public class CodeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //get 请求和 post 请求一样，直接交给post处理，回调doPost
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //post 请求中写业务功能
        //几板斧 哈哈
        //resp.setContentType("text/html;charset=utf-8");
        int width=120;
        int height=30;
        //创建一个内存中带缓存的图片对象
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
        //获取2D画笔对象
        Graphics g=image.getGraphics();
        //给画笔对象设置颜色，为图片喷绘背景色
        g.setColor(Color.white);
        g.fillRect(0,0,width,height);
        //设置颜色，绘制图片边框
        g.setColor(Color.black);
        g.drawRect(1,1,width-2,height-2);


        //先使用一个常量字符串测试程序设计思路 String
        String yzm="";
        //想办法将yzm，在0-9和a-z、A-Z中随机组合4个字符的字符串  定义一个字符仓库
        String baseChar="0123";
        //随机数对象
        Random rd=new Random();
        for (int i = 0; i <4; i++) {
            int pos=rd.nextInt(baseChar.length());
            yzm=yzm+baseChar.charAt(pos);

        }
        g.setColor(Color.RED);  //红色画笔
        g.setFont(new Font("宋体",Font.ITALIC,20));
        g.drawString(yzm,15,22);  //绘制文字到画布
        //绘制一些干扰线
        /*
         * 在图片上画随机线条
         * @param g
         * */
        g.setColor(Color.blue);
        for (int i = 0; i < 10; i++) {
            int x1= rd.nextInt(width);
            int y1=rd.nextInt(height);
            int x2= rd.nextInt(width);
            int y2= rd.nextInt(height);
            g.drawLine(x1,y1,x2,y2);
        }

        HttpSession session=req.getSession();
        session.setAttribute("code",yzm);

        //给客户端设置MINE类型
        resp.setHeader("content-type","image/jpeg");   //******
        //使用响应对象的字节输出流，写到客户端浏览器
        //设置响应头控制浏览器不要缓存
        resp.setDateHeader("expries", -1);
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Pragma", "no-cache");

        ImageIO.write(image,"jpg",resp.getOutputStream());
    }
}
