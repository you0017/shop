package com.yc.web.listener;

import com.yc.bean.Comment;
import com.yc.bean.Item;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.*;

@WebListener
//应用启动时，记录当前系统的信息
public class SystemInfoListener_reception implements ServletContextListener {
    Timer timer1;    //全局变量，方便销毁
    Timer timer2;    //全局变量，方便销毁
    @Override//容器初始化，在应用程序  在服务中加载时调用
    public void contextInitialized(ServletContextEvent sce) {
        //记录系统信息
        Properties properties = System.getProperties();

        System.out.println("应用程序启动时间："+new Date());
        System.out.println(properties.get("os.name"));
        System.out.println(properties.get("os.version"));
        System.out.println("空闲内存大小："+Runtime.getRuntime().freeMemory());

        //启动一个Timer定时任务（设置为后台线程，主线程tomcat销毁，定时任务就销毁
        timer1 = new Timer(true);//true设置为后台线程， 默认精密线程，不销毁

        //设置第一次执行是明天的00:00:00
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE,1);
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        //算评分和优惠券过期的
        timer1.schedule(new MyTimerTask1(),c.getTime(),24*60*60*1000);//明天0点开始，间隔1天再次执行


        timer2 = new Timer(true);
        c = Calendar.getInstance();
        c.add(Calendar.DATE,1);
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        //热门产品的
        timer2.schedule(new MyTimerTask2(),c.getTime(),7*24*60*60*1000);//7天再次执行
    }

    @Override//销毁方法，在服务器关闭时调用
    public void contextDestroyed(ServletContextEvent sce) {
        if (timer1!=null){
            timer1.cancel();
        }
        if (timer2!=null){
            timer2.cancel();
        }
    }
}

/**
 * 这个是每天凌晨算一次所有的评分
 */
class MyTimerTask1 extends TimerTask{

    @Override
    public void run() {
        DBHelper db = new DBHelper();
        try {
            //这个是用来算评分的
            List<Item> select = db.select(Item.class, "select * from item");//查所有的商品
            for (Item item : select) {
                //遍历所有商品，及其评论
                List<Comment> select1 = db.select(Comment.class, "select * from comments where item_id=?", item.getId());
                int total=0;
                Integer o = Integer.valueOf(db.select("select count(*) from comments where item_id=?", item.getId()).get(0).get("count(*)").toString());
                if (o.equals(0)){
                    return;
                }
                for (Comment comment : select1) {
                    total += comment.getRating();
                }
                int rating = total/o;
                //平均分弄进去
                db.doUpdate("update item set rating=?",rating);
            }


            //这个是用来算优惠券过期的
            db.doUpdate("UPDATE coupons  SET status = 'expired' WHERE expiration_date <= CURRENT_DATE;");

            //这个是用来算用户优惠券过期的
            db.doUpdate("UPDATE user_coupon  SET used = 2 WHERE expiration_date <= CURRENT_DATE;");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class MyTimerTask2 extends TimerTask{

    @Override
    public void run() {
        DBHelper db = new DBHelper();
        String sql = "select * from item where status = 1 order by sold desc limit 8 offset 0";
        try {
            Jedis redis = RedisHelper.getRedisInstance();
            List<Item> select = db.select(Item.class, sql);
            redis.set("hot",select.toString());
        } catch (Exception e) {
            throw new RuntimeException("热门异常");
        }
    }
}