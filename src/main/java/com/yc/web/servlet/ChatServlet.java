package com.yc.web.servlet;

import com.yc.bean.Chat;
import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.WebSocket;
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

@WebServlet("/html/chat.action")
public class ChatServlet extends BaseServlet {

    private DBHelper db = new DBHelper();

    private Timer timer;

    /**
     * 清除这次聊天记录
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void clear(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.del(YcConstants.CHAT+user_id);
        redis.close();
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 发送
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void send(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String message = req.getParameter("message");
        JsonModel jm = new JsonModel();
        if (id==null||id.equals("")){
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }

        /**
         * 键 - 时间戳 - 内容
         */
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
        if (redis.exists(YcConstants.CHAT+id)){
            redis.zadd(YcConstants.CHAT+id,System.currentTimeMillis(),formattedDate+"^*^"+name+"*^*"+message);
        }else{
            redis.zadd(YcConstants.CHAT+id,System.currentTimeMillis(),formattedDate+"^*^"+name+"*^*"+message);
            long a = 60*60*24;
            redis.expire(YcConstants.CHAT+id,a);
        }
        redis.close();


        jm.setCode(1);
        writeJson(jm,resp);

        //已经把消息存到redis了
        //现在连接webSocket给后台的前端发送消息
        //String url = generateUrl(req);
        WebSocket echoServer = new WebSocket();
        echoServer.send(id);
        //给连接我的这个前台发送id，告诉要刷新哪一个用户了
    }

    /*private String generateUrl(HttpServletRequest request){
        String protocol = request.isSecure() ? "wss" : "ws";
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        String endpoint = "/shop_war/echo";

        String wsUrl = String.format("%s://%s:%d%s%s", protocol, host, port, contextPath, endpoint);
        return wsUrl;
    }*/


    protected void get(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        JsonModel jm = new JsonModel();
        //未登录
        if (id==null || id.equals("")){
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }
        long zcard = redis.zcard(YcConstants.CHAT + id);
        List<String> message = redis.zrange(YcConstants.CHAT + id, 0, zcard + 1);
        //System.out.println(message);

        // 关闭连接
        redis.close();
        List<UserInformation> select = db.select(UserInformation.class, "select * from userinformation where id=?", id);
        String name = select.get(0).getName();
        List<Chat> chats = new ArrayList<>();
        Chat chat = null;
        for (String m : message) {
            chat = new Chat();
            chat.setTime(m.substring(0,m.indexOf("^*^")));
            chat.setSender(m.substring(m.indexOf("^*^")+3,m.indexOf("*^*")));
            chat.setMessage(m.substring(m.indexOf("*^*")+3));
            chats.add(chat);
        }

        jm.setCode(1);
        jm.setObj(chats);
        writeJson(jm,resp);
    }
}
