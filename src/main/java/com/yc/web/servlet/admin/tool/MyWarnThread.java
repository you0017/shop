package com.yc.web.servlet.admin.tool;

import com.yc.dao.DBHelper;
import com.yc.web.servlet.admin.admutils.QQMailUtil;
import lombok.Data;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

@Data
public class MyWarnThread extends TimerTask {
    private String place;  // 预警位置
    private String warn_name;
    private String qqMail;
//    private String startTime = request.getParameter("start_time");  // 预警开始时间
//    private String week = request.getParameter("week");  // 预警周几
//    private String specific_time = request.getParameter("specific_time");  // 具体时间


    public MyWarnThread() {
    }

    public MyWarnThread(String place, String warn_name, String qqMail) {
        this.place = place;
        this.warn_name = warn_name;
        this.qqMail = qqMail;
    }

    @Override
    public void run() {
        DBHelper dbHelper = new DBHelper();
        QQMailUtil qqMailUtil = new QQMailUtil();

        // 测试
//        String sql = "select name from item where stock < stock_down";
//        List<Map<String, Object>> maps = dbHelper.select(sql);
//        String temp = "";
//        if (maps != null && maps.size() > 0) {
//            for (Map<String, Object> map : maps) {
//                temp += map.get("name") + "     ";
//            }
//        }
//        System.out.println(temp);
        // 正式
        try {
            System.out.println("开始执行");
            String sql = "select name from item where stock < stock_down";
            List<Map<String, Object>> maps = dbHelper.select(sql);
            String temp = "";
            if (maps != null && maps.size() > 0) {
                for (int i = 0; i < maps.size()-1; i++) {
                    temp += maps.get(i).get("name") + " , ";
                }
//                for (Map<String, Object> map : maps) {
//                    temp += map.get("name") + "  ";
//                }
                String o = (String) maps.get(maps.size()-1).get("name");
                temp += o;
                qqMailUtil.send(" 尊敬的" + warn_name + "老板，您位于 '" + place + "' 的仓库商品:" + temp + "的库存不足，请及时补货！", qqMail);
            } else {
                qqMailUtil.send(" 尊敬的" + warn_name + "老板，您位于 '" + place + "' 的商品库存充足！", qqMail);
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("任务执行了");
    }
}
