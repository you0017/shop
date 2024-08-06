package com.yc.web.listener;


import com.yc.bean.Chat;
import com.yc.dao.RedisHelper;

import com.yc.utils.AdminEchoServer;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.*;

//@WebListener
//应用启动时，记录当前系统的信息
public class SystemInfoListener  implements ServletContextListener  {
    Timer timer;    //全局变量，方便销毁
    @Override//容器初始化，在应用程序  在服务中加载时调用
    public void contextInitialized(ServletContextEvent sce) {
        //记录系统信息
        //启动一个Timer定时任务（设置为后台线程，主线程tomcat销毁，定时任务就销毁
        timer = new Timer(true);//true设置为后台线程， 默认精密线程，不销毁

        long l = System.currentTimeMillis();
        //设置第一次执行是明天的00:00:00
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        System.out.println(date);
//        date.getTime()
//        c.add(Calendar.DATE,1);
//        c.set(Calendar.HOUR_OF_DAY,0);
//        c.set(Calendar.MINUTE,0);
//        c.set(Calendar.SECOND,0);
//        c.set(l);
        timer.schedule(new MyTimerTask(),date,2000);//明天0点开始，间隔1天再次执行
    }

    @Override//销毁方法，在服务器关闭时调用
    public void contextDestroyed(ServletContextEvent sce) {
        if (timer!=null){
            timer.cancel();
        }
    }
}

class MyTimerTask extends TimerTask {

    @Override
    public void run() {
        AdminEchoServer echoServer = new AdminEchoServer();
        Jedis jedis = RedisHelper.getRedisInstance();
        // 1、获取所有用户消息   2、排序  3、取前10条   4、发送给客户端
//        keys chat_*    拿到所有chat_开头的key  拿到用户
        Set<String> keys = jedis.keys("chat_*"); // 拿到所有用户
        Object[] array = keys.toArray();  // 转成数组
        List allChats = new ArrayList<>();
        for (Object o : array) {
            List<Chat> chats = new ArrayList<>();
            String name = o.toString();  // 拿到用户名键名
            long userLength = jedis.zcard(name); // 聊天记录个数
            List<String> list = jedis.zrange(name,0,userLength);  // 每个用户聊天记录组成个集合
            Chat  chat=null;
            for (String m : list) {
                chat = new Chat();
                chat.setTime(m.substring(0,m.indexOf("^*^")));
                chat.setSender(m.substring(m.indexOf("^*^")+3,m.indexOf("*^*")));
                chat.setMessage(m.substring(m.indexOf("*^*")+3));
                chats.add(chat);
            }
            allChats.add(chats);
        }

    }
}