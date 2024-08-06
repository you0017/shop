package com.yc.web.servlet;

import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.*;
import com.yc.web.model.JsonModel;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/html/user.action")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class UserServlet extends BaseServlet{
    private DBHelper db = new DBHelper();

    /**
     * 发送验证码
     * @param req
     * @param resp
     */
    protected void sendCode(HttpServletRequest req,HttpServletResponse resp) throws Exception {
        String mobile = req.getParameter("mobile");
        SendSms sendSms = new SendSms();
        sendSms.send(req,mobile);
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        writeJson(jm,resp);
    }


    /**
     * 修改头像
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void modifyImg(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
        Part image = req.getPart("image");
        AliOSSUtils aliOSSUtils = new AliOSSUtils(new AliOSSProperties());
        String url=null;
        try {
            url = aliOSSUtils.upload(image);
        } catch (Exception e) {
            throw new RuntimeException("头像修改失败");
        }
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.close();
        JsonModel jm = new JsonModel();
        if (user_id==null){
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }
        db.doUpdate("update userinformation set image = ?",url);
        jm.setCode(1);
        jm.setObj(url);
        writeJson(jm,resp);
    }

    /**
     * 修改
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void modify(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        //String username = req.getParameter("username");
        String password = req.getParameter("password");
        String code = req.getParameter("code");
        //String name = req.getParameter("name");

        JsonModel jm = new JsonModel();

        //看看登录了嘛
        Jedis redis = RedisHelper.getRedisInstance();
        String id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        if (id==null||id.equals("")||id.length()<=0){
            jm.setCode(0);
            jm.setError("未登录，修改不了");
            writeJson(jm,resp);
            return;
        }

        if (redis.get(YcConstants.MESSAGE + req.getSession().getId())==null||!redis.get(YcConstants.MESSAGE + req.getSession().getId()).equals(code)){
            jm.setCode(0);
            jm.setError("验证码错误");
            writeJson(jm,resp);
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.now();

        List params = new ArrayList();
        params.add(String.valueOf(localDateTime));
        String sql = "update userinformation set updatetime=? ";


        /*if (username!=null&&!username.equals("")){
            params.add(username);
            sql += ",accountname=? ";
        }*/


        if (password!=null&&!password.equals("")){
            password = EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password));
            params.add(password);
            sql += ",password=? ";
        }


        /*if (name!=null&&!name.equals("")){
            params.add(name);
            sql+=",name=? ";

        }*/
        sql += "where id=?";
        params.add(id);

        int i = db.doUpdate(sql, params.toArray());
        if (i==0){
            jm.setCode(0);
            jm.setError("修改失败");
            writeJson(jm,resp);
            return;
        }

        //修改成功记得改一下redis
        //退出登录
        logout(req, resp);

        redis.close();
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 登出
     * @param req
     * @param resp
     */
    protected void logout(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        //删掉登录状态
        redis.del(YcConstants.SHOP_USERID+req.getSession().getId());
        redis.close();
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 查看登录状态
     * @param req
     * @param resp
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected void checkLogin(HttpServletRequest req,HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException{
        JsonModel jm = new JsonModel();

        //redis查看是否登录
        HttpSession session = req.getSession();
        Jedis redis = RedisHelper.getRedisInstance();
        String id = redis.get(YcConstants.SHOP_USERID + session.getId());
        if (id==null||id.length()<=0||id.equals("")){
            //未登录
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }
        String sql = "select * from userinformation where id = ?";
        List<UserInformation> select = db.select(UserInformation.class, sql, id);

        //登录了
        jm.setCode(1);
        jm.setObj(select.get(0));
        writeJson(jm,resp);

        redis.close();
    }

    /**
     * 登录
     * @param req
     * @param resp
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */

    protected void login(HttpServletRequest req,HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        //参数
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        JsonModel jm = new JsonModel();

        String sql = "select * from userinformation where accountname = ?";
        List<UserInformation> user = db.select(UserInformation.class, sql, username);

        //看注册了没有
        if (user==null||user.size()<=0){
            jm.setCode(0);
            jm.setError("用户不存在，请注册");
            writeJson(jm,resp);
            return;
        }

        //看是否处于封禁
        UserInformation userInformation = user.get(0);
        if (userInformation.getStatus().equals(0)){
            jm.setCode(0);
            jm.setError("用户被封禁");
            writeJson(jm,resp);
            return;
        }

        //看密码是否正确
        if (!userInformation.getPassword().equals(EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password)))){
            jm.setCode(0);
            jm.setError("密码错误");
            writeJson(jm,resp);
            return;
        }

        //登录成功
        //登录次数+1
        sql = "update userinformation set logincount=logincount+1 where id=?";
        int i = db.doUpdate(sql, userInformation.getId());

        //存入redis，看登录状态
        Jedis jedis = RedisHelper.getRedisInstance();

        //考虑不同电脑和浏览器问题，用session当名字存
        HttpSession session = req.getSession();
        String id = session.getId();

        jedis.set(YcConstants.SHOP_USERID +id, String.valueOf(userInformation.getId()));

        jedis.close();

        jm.setCode(1);
        jm.setObj(userInformation.getId());
        writeJson(jm,resp);

    }

    /**
     * 注册
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected void register(HttpServletRequest req,HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException, ServletException {
        //获取基本参数
        String username = req.getParameter("username");
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String tel = req.getParameter("tel");
        String password = req.getParameter("password");
        String code = req.getParameter("code");
        String mobile = req.getParameter("tel");
        JsonModel jm = new JsonModel();

        Jedis redis = RedisHelper.getRedisInstance();
        if (redis.get(YcConstants.MESSAGE + req.getSession().getId())==null||!redis.get(YcConstants.MESSAGE + req.getSession().getId()).equals(code)){
            jm.setCode(0);
            jm.setError("验证码错误");
            writeJson(jm,resp);
            return;
        }
        Part image = req.getPart("image");

        AliOSSUtils aliOSSUtils = new AliOSSUtils(new AliOSSProperties());
        String url = aliOSSUtils.upload(image);

        if (username==null||username.equals("")||name==null||name.equals("")||email.equals("")||email==null||tel.equals("")||tel==null||password.equals("")||password==null){
            jm.setCode(0);
            jm.setError("请填写完整");
            writeJson(jm,resp);
            return;
        }



        //看看用户是否存在
        String sql = "select * from userinformation where accountname = ?";
        List<UserInformation> user = db.select(UserInformation.class, sql, username);
        if (user.size()>0){
            jm.setCode(0);
            jm.setError("此用户已存在");
            writeJson(jm,resp);
            return;
        }

        //加密
        password = EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password));

        UserInformation userInformation = new UserInformation();
        userInformation.setAccountname(username);
        userInformation.setPassword(password);
        userInformation.setName(name);
        userInformation.setStatus("1");
        userInformation.setEmail(email);
        userInformation.setLogincount(0);
        userInformation.setRole(String.valueOf(1));
        userInformation.setMobile(mobile);
        userInformation.setImage(url);

        LocalDateTime now = LocalDateTime.now();
        userInformation.setCreationtime(String.valueOf(now));
        userInformation.setUpdatetime(String.valueOf(now));

        sql = "INSERT INTO userinformation (AccountName, Password, Name, Status, Email, LoginCount, CreationTime, UpdateTime,role,mobile,image)\n" +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        int i = db.doUpdate(sql, userInformation.getAccountname(), userInformation.getPassword()
                , userInformation.getName(), userInformation.getStatus()
                , userInformation.getEmail(),userInformation.getLogincount()
                , userInformation.getCreationtime(), userInformation.getUpdatetime()
                ,userInformation.getRole(),userInformation.getMobile(),url);

        if (i==0){
            //新增失败
            jm.setCode(0);
            jm.setError("注册失败，未知错误");
            writeJson(jm,resp);
            return;
        }

        //新增成功,发送邮箱
        Sendmail sendmail = new Sendmail(userInformation);
        sendmail.start();

        jm.setCode(1);
        //jm.setObj();
        writeJson(jm,resp);
    }
}
