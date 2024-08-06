package com.yc.web.servlet.admin.admservlet;


import com.aliyuncs.exceptions.ClientException;
import com.yc.bean.AdminUser;
import com.yc.utils.EncryptUtils;
import com.yc.web.model.DataModel;
import com.yc.web.servlet.admin.admutils.SMSUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/register.action")
public class RegisterServlet extends BaseServlet {
    // 注册功能
    public void register(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
//        System.out.println("来注册了");
        // 1、拿出数据转为对象  2、取出session里面的验证码  3、取出request中的验证码，判断与后台发出的验证码是否一样
        // 4、判断是否已存在用户   5、添加用户，密码要加盐  6、返回结果信息
        DataModel ud = new DataModel();
        AdminUser register = super.parseObjectFromRequest(request, AdminUser.class);
        String captcha = request.getParameter("captcha");   // 前端传来的验证码
        HttpSession session = request.getSession();
        String sessionVerifCode = (String) session.getAttribute("verifCode");  // 后台发出的验证码
//        System.out.println("session里面的发出去验证码为：" + sessionVerifCode);
        if (!sessionVerifCode.equals(captcha)) {   // 如果前台和后台的验证码不一样
            ud.setCode(1);
            ud.setMsg("验证码错误");
            writeJson(ud, response);
            return;
        }
        // 检查是否有相同的了用户   名字 角色都相同
        String sqlCheck = "select * from adminuser where username =? and role = ? ";
        List<Map<String, Object>> maps = db.select(sqlCheck, register.getUsername(), register.getRole());
        if (maps.size() >= 1) {   // 如果查出相同的了
            ud.setCode(1);
            ud.setMsg("有相同的账号了");
            writeJson(ud, response);
            return;
        }
        register.setPassword(EncryptUtils.encryptToMD5(register.getPassword()));  // 密码加盐
        // 可以添加了
        //insert into users (username, password, role, creationtime, phone)
        // values ('书本', 'password', 'role', current_timestamp, 'phone');
        String addSql = "insert into adminuser (username, password, role, creationtime, phone) " +
                "values ( ?,    ?,   ?,  current_timestamp,  ? ) ";
        int registeResult = db.doUpdate(addSql, register.getUsername(), register.getPassword(),
                register.getRole(), register.getPhone());
        if ( registeResult>=1){ // 注册成功了
            ud.setCode(0);
            ud.setMsg("注册成功");
        }else {
            ud.setCode(1);
            ud.setMsg("注册失败，请检查所填信息");
        }
        writeJson(ud, response);
    }

    // 短信验证码功能
    public void verifCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //  System.out.println("来要验证码了");
        // 1、拿个4位数随机数 2、存到session里面  2、拿到手机号   3、发送到手机  4、返回获取结果
        SMSUtil smsUtil = new SMSUtil();
        DataModel ud = new DataModel();
        HttpSession session = request.getSession();

        String verifCode = smsUtil.random();  // 验证码
        session.setAttribute("verifCode", verifCode);
        String phone = request.getParameter("phone");
        Boolean sendSMSResult = null;
        try {
            sendSMSResult = smsUtil.sendSMS(phone, verifCode);
        } catch (ClientException e) {
            System.out.println("后台发送验证码出错");
            throw new RuntimeException(e);
        }
        if (sendSMSResult == true) {
            ud.setCode(0);
            ud.setMsg("获取成功，请查看手机");
        } else {
            ud.setCode(1);
            ud.setMsg("获取失败，请检查所填信息");
        }
        writeJson(ud, response);
    }

}
