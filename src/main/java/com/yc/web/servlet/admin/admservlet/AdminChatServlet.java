package com.yc.web.servlet.admin.admservlet;

import com.yc.bean.Chat;
import com.yc.bean.UserInformation;
import com.yc.dao.RedisHelper;
import com.yc.utils.AdminEchoServer;
import com.yc.utils.YcConstants;
import com.yc.web.model.JsonModel;

import redis.clients.jedis.Jedis;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/admin/chat.action")
public class AdminChatServlet extends BaseServlet {
    private Timer timer;

    // 初始化读取所有消息
    public void getAllChat(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        Jedis jedis = RedisHelper.getRedisInstance();
        JsonModel jm = new JsonModel();
        Set<String> keys = jedis.keys("chat_*"); // 拿到所有用户
        Object[] array = keys.toArray();  // 转成数组
        List allChats = new ArrayList<>();
        for (Object o : array) {
            List<Chat> chats = new ArrayList<>();
            String name = o.toString();  // 拿到用户名键名
            long userLength = jedis.zcard(name); // 聊天记录个数
            List<String> list = jedis.zrange(name, 0, userLength);  // 每个用户聊天记录组成个集合
            Chat chat = null;
            for (String m : list) {
                chat = new Chat();
                chat.setTime(m.substring(0, m.indexOf("^*^")));
                chat.setSender(m.substring(m.indexOf("^*^") + 3, m.indexOf("*^*")));
                chat.setMessage(m.substring(m.indexOf("*^*") + 3));
                chats.add(chat);
            }
            allChats.add(chats);
        }
        jm.setCode(1);
        jm.setObj(allChats);
        writeJson(jm, resp);
    }


    /**
     * 清除这次聊天记录
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void clear(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.del(YcConstants.CHAT + user_id);
        redis.close();
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        writeJson(jm, resp);
    }

    /**
     * 发送
     * @param req
     * @param resp
     * @throws IOException
     */
    public void setChatMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        String newMessage = req.getParameter("newMessage");
        String sender = req.getParameter("sender");
        String sql = "select id from userinformation where accountname=?";
        List<UserInformation> senderId = db.select(UserInformation.class, sql, sender);   // 用户的id号的集合
        String id = senderId.get(0).getId().toString();    // 用户id

        Jedis redis = RedisHelper.getRedisInstance();
        // 获取当前时间的毫秒数
        long currentTimeMillis = System.currentTimeMillis();
        // 创建一个 Date 对象
        Date date = new Date(currentTimeMillis);
        // 定义时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将 Date 对象格式化为字符串
        String formattedDate = sdf.format(date);

        // 打印结果
        //System.out.println("当前时间的标准格式: " + formattedDate);
        redis.zadd(YcConstants.CHAT + id, System.currentTimeMillis(), formattedDate + "^*^" + "店家" + "*^*" + newMessage);
        redis.close();

        JsonModel jm = new JsonModel();
        jm.setCode(1);
        writeJson(jm, resp);

        // 通知用户
        AdminEchoServer echo = new AdminEchoServer();
        echo.send("1");
    }

    protected void get(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        JsonModel jm = new JsonModel();
        //未登录
        if (id == null || id.equals("")) {
            jm.setCode(0);
            writeJson(jm, resp);
            return;
        }
        long zcard = redis.zcard(YcConstants.CHAT + id);
        List<String> message = redis.zrange(YcConstants.CHAT + id, 0, zcard + 1);
        System.out.println(message);
        // 关闭连接
        redis.close();
        List<UserInformation> select = db.select(UserInformation.class, "select * from userinformation where id=?", id);
        String name = select.get(0).getName();
        List<Chat> chats = new ArrayList<>();
        Chat chat = null;
        for (String m : message) {
            chat = new Chat();
            chat.setTime(m.substring(0, m.indexOf("^*^")));
            chat.setSender(m.substring(m.indexOf("^*^") + 3, m.indexOf("*^*")));
            chat.setMessage(m.substring(m.indexOf("*^*") + 3));
            chats.add(chat);
        }
        jm.setCode(1);
        jm.setObj(chats);
        writeJson(jm, resp);
    }

    // 根据id获取记录
    public void getMessageById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jm = new JsonModel();
        String id = request.getParameter("id");   // 用户id
        Jedis jedis = RedisHelper.getRedisInstance();   // 获取redis连接
        String userKey = YcConstants.CHAT + id;
        long userLength = jedis.zcard(userKey);    // 聊天记录长度
        List<String> message = jedis.zrange(YcConstants.CHAT + id, userLength-1, userLength-1);  // 获取最新一条聊天记录
        // 关闭连接
        jedis.close();
        Chat msg = null;
        for (String m : message) {
            msg = new Chat();
            msg.setTime(m.substring(0, m.indexOf("^*^")));
            msg.setSender(m.substring(m.indexOf("^*^") + 3, m.indexOf("*^*")));
            msg.setMessage(m.substring(m.indexOf("*^*") + 3));
        }
        jm.setCode(1);
        jm.setObj(msg);
        writeJson(jm, response);
    }


}
