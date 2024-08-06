package com.yc.web.servlet.admin.admservlet;

import com.yc.bean.AdminUser;
import com.yc.bean.Menu;
import com.yc.utils.EncryptUtils;
import com.yc.web.model.DataModel;
import com.yc.web.model.JsonModel;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/login.action")
//@WebServlet("/login.action")
public class LoginServlet extends BaseServlet {

    // 登录信息
    public void getInfo(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonModel jsonModel = new JsonModel();
        HttpSession session = request.getSession();
        AdminUser username = (AdminUser) session.getAttribute("sell");
        jsonModel.setObj(username.getUsername());
        jsonModel.setCode(1);
        writeJson(jsonModel, response);
    }

    // 退出
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("我在退出");
        DataModel ud = new DataModel();
        HttpSession session = request.getSession();
        session.removeAttribute("sellUser");
        session.removeAttribute("adminUser");
        ud.setCode(0);
        writeJson(ud, response);
    }

    // 每个角色显示什么菜单
    public void show(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // session里面有role
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role"); //
        DataModel ud = new DataModel();
        List<Menu> list = new ArrayList<>();
        if (role!=null && Integer.parseInt(role) == 1) {  // 是商家
            System.out.println("商家来了");
            Menu m = new Menu();
            m.setTitle("控制台");
            m.setHref("pages/console.html");
            m.setFontFamily("ok-icon");
            m.setIcon("&#xe654;");
            m.setSpread(true);
            m.setIsCheck(true);
            list.add(m);
            Menu m2 = new Menu();
            m2.setTitle("商品管理");
            m2.setHref("pages/often/product.html");
            m2.setFontFamily("");
            m2.setIcon("&#xe62e;");
            m2.setSpread(true);
            m2.setIsCheck(false);
            list.add(m2);
            Menu m3 = new Menu();
            m3.setTitle("所有订单");
            m3.setHref("pages/order/sale_statistics.html");
            m3.setFontFamily("");
            m3.setIcon("&#xe62e;");
            m3.setSpread(true);
            m3.setIsCheck(false);
            list.add(m3);
            Menu m4 = new Menu();
            m4.setTitle("发货处理");
            m4.setHref("pages/order/order_consign.html");
            m4.setFontFamily("");
            m4.setIcon("&#xe62e;");
            m4.setSpread(true);
            m4.setIsCheck(false);
            list.add(m4);
            Menu m5 = new Menu();
            m5.setTitle("回收站");
            m5.setHref("pages/order/order_recover.html");
            m5.setFontFamily("");
            m5.setIcon("&#xe62e;");
            m5.setSpread(true);
            m5.setIsCheck(false);
            list.add(m5);
        } else if (role!=null &&Integer.parseInt(role) == 2) {
            Menu m = new Menu();
            m.setTitle("仪表盘");
            m.setHref("pages/console1.html");
            m.setFontFamily("ok-icon");
            m.setIcon("&#xe665;");
            m.setSpread(true);
            m.setIsCheck(true);
            list.add(m);
            Menu m2 = new Menu();
            m2.setTitle("用户列表");
            m2.setHref("pages/member/user-list.html");
            m2.setFontFamily("layui-icon");
            m2.setIcon("&#xe62e;");
            m2.setSpread(false);
            m2.setIsCheck(false);
            list.add(m2);
        } else {
            ud.setMsg("不给");
            ud.setCode(1);
        }
//        writeJson(ud,response);
        writeObjectJson(list, response);
    }

    // 登录功能    msg:商家登录返回 sell   平台 admin
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("login begin");
        DataModel ud = new DataModel();
        AdminUser adminUser = null;
        try {
            adminUser = super.parseObjectFromRequest(request, AdminUser.class);
            String captcha = request.getParameter("captcha");  // 用户填的验证码
            HttpSession session = request.getSession();

            // 到时候根据session里的role给前端返回菜单
            String role = request.getParameter("role");  // 前台传来的role
            session.setAttribute("role", role);
            System.out.println(role);
            Boolean flag = false;  // 判断前台的role和库里的role是否一样
            String code = (String) session.getAttribute("code");  // 取code时的
            if (!captcha.equals(code)) {   // 如果验证码不对
                ud.setCode(1);
                ud.setMsg("验证码错误");
                super.writeJson(ud, response);
                return;
            }
            adminUser.setPassword(EncryptUtils.encryptToMD5(adminUser.getPassword()));  //加盐
            String sql = "select * from adminuser where username=? and password=? and role=1";  // 查商家
            String sql1 = "select * from adminuser where username=? and password=? and role=2";  // 查平台
            List<AdminUser> sellList = db.select(AdminUser.class, sql, adminUser.getUsername(), adminUser.getPassword());
            List<AdminUser> adminList = db.select(AdminUser.class, sql1, adminUser.getUsername(), adminUser.getPassword());
            if (sellList != null && sellList.size() > 0) {  // 表示查到商家
                AdminUser sell = sellList.get(0);
                sell.setPassword("别偷看");
                String sellRole = sell.getRole();  // "1"
                if ( sellRole.equals(role) ) {   // 如果查到的role和前台传来的role匹配
                    ud.setCode(0);
                    ud.setData(sell);
                    ud.setMsg("sell");
                    // 登录信息记录到session中
                    session.setAttribute("sell", sell);
                }else {  // 不匹配
                    ud.setCode(1);
                    ud.setMsg("你确定你角色选对了？");
                }
            } else if (adminList != null && adminList.size() > 0) {   // 表示查到admin
                AdminUser admin = adminList.get(0);
                admin.setPassword("别偷看");
                String adminRole = admin.getRole();  // "2"
                if ( adminRole.equals(role) ) { // 如果查到的role和前台传来的role匹配
                    ud.setCode(0);
                    ud.setData(admin);
                    ud.setMsg("admin");
                    session.setAttribute("admin", admin);
                }else {
                    ud.setCode(1);
                    ud.setMsg("你确定你角色选对了？");
                }

            } else {  // 啥都没有
                ud.setCode(1);
                ud.setMsg("用户名或密码错误，登陆失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ud.setCode(1);
            ud.setMsg(e.getClass().toString());
        }
        writeJson(ud, response);
    }


}
