package com.yc.web.servlet.admin.admservlet;

import com.yc.bean.Item;
import com.yc.dao.DBHelper;
import com.yc.utils.Sendmail;
import com.yc.web.model.DataModel;
import com.yc.web.servlet.admin.tool.MyWarnThread;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@WebServlet("/admin/inventory.action")
public class InventorySverlet extends BaseServlet {

    // 预警信息
    public void setTime(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        System.out.println("there");
        String place = request.getParameter("place");  // 预警位置
        String warn_name = request.getParameter("warn_name"); // 通知谁
        String qqMail = request.getParameter("qq_mail"); // qq邮箱
        String startTime = request.getParameter("start_time");  // 预警开始时间
        String week = request.getParameter("week");  // 预警周几
        int week_num = Integer.parseInt(week);  // 周几的int型
        String specific_time = request.getParameter("specific_time");  // 具体时间
        // 解析日期字符串
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null;   // 预警开始时间
            startDate = formatter.parse(startTime);   // 预警开始时间
            // 计算距离下一个周二的时间间隔
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            // 如果不是我要的周几，则一直循环，直到找到我需要的周几
            while (calendar.get(Calendar.DAY_OF_WEEK) != week_num) {
//                System.out.println(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            LocalTime time = LocalTime.parse(specific_time);
            // 具体定时的毫秒数
            long milliseconds = ChronoUnit.MILLIS.between(LocalTime.MIDNIGHT, time);
            calendar.add(Calendar.MILLISECOND, (int) milliseconds);
            Date nextTuesday = calendar.getTime();
            Timer timer = new Timer();
            long period = 7 * 24 * 60 * 60 * 1000; // 一周的毫秒数
            MyWarnThread myWarnThread = new MyWarnThread(place, warn_name, qqMail);   // 任务线程
            timer.schedule(myWarnThread, nextTuesday, period);
            ud.setCode(0);
            ud.setMsg("设置成功");
            writeJson(ud, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    // 批量预警
    public void batchWarn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String form = request.getParameter("form");
        String form1_stock_down = request.getParameter("form1_stock_down");
        String form1_stock_up = request.getParameter("form1_stock_up");
        String form2_stock_down = request.getParameter("form2_stock_down");
        String form2_stock_up = request.getParameter("form2_stock_up");
        String idsStr = request.getParameter("idsStr");  // 商品编码
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = null;
        if ("form1".equals(form)) {  // 证明是一件固定预警
            sql = new StringBuffer("update item set stock_down=" + form1_stock_down + ",stock_up=" + form1_stock_up + " ,warning_value_status=1 WHERE ID IN ( ");
            for (int i = 0; i < intArray.length - 1; i++) {
                sql.append(" ?,");
            }
            sql.append(" ?");
            sql.append(")");
        } else if ("form2".equals(form)) {  // 证明是一键动态预警
            sql = new StringBuffer("update item set stock_down=200,stock_up=500 ,warning_value_status=2 where id in ( ");
            for (int i = 0; i < intArray.length - 1; i++) {
                sql.append(" ?,");
            }
            sql.append(" ?");
            sql.append(")");
        }
        int result = db.doUpdate(sql.toString(), intArray);
        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("预警成功");
        } else {
            ud.setCode(1);
            ud.setMsg("预警失败");
        }
        writeJson(ud, response);
    }

    // 一键预警
    public void editAllLimit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String form = request.getParameter("form");
        String id = request.getParameter("id");  // 商品编码
        String form1_stock_down = request.getParameter("form1_stock_down");
        String form1_stock_up = request.getParameter("form1_stock_up");
        String form2_stock_down = request.getParameter("form2_stock_down");
        String form2_stock_up = request.getParameter("form2_stock_up");
        if ("form1".equals(form)) {  // 证明是一件固定预警
            String sql = "update item set stock_down=?,stock_up=? ,warning_value_status=1 where 1=1 ";
            int result = db.doUpdate(sql, Integer.parseInt(form1_stock_down), Integer.parseInt(form1_stock_up));
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("一键设置成功");
            } else {
                ud.setCode(1);
                ud.setMsg("一键设置失败");
            }
        } else if ("form2".equals(form)) {  // 证明是一键动态预警
            String sql = "update item set stock_down=200,stock_up=500 ,warning_value_status=2 where 1=1 ";
            int result = db.doUpdate(sql);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("一键设置成功");
            } else {
                ud.setCode(1);
                ud.setMsg("一键设置失败");
            }
        } else {
            ud.setCode(1);
            ud.setMsg("一键设置失败");
        }
        writeJson(ud, response);
    }

    // 动态监控库存
    public void realLook(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        System.out.println("realLook");
        DataModel ud = new DataModel();
        String sql = "select * from item where warning_value<=stock ";
        List<Map<String, Object>> maps = db.select(sql);
        if (maps.size() >= 1) {
            ud.setCode(0);
            ud.setMsg("库存预警");
        } else {
            ud.setCode(1);
            ud.setMsg("库存正常");
        }
        writeJson(ud, response);

    }


    // 设置固定预警上下限
    public void editLimit(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        System.out.println("setLimit");
        HttpSession session = request.getSession();
        DataModel ud = new DataModel();
        String form = request.getParameter("form");
        String id = request.getParameter("id");  // 商品编码
        String form1_stock_down = request.getParameter("form1_stock_down");
        String form1_stock_up = request.getParameter("form1_stock_up");
        String form2_stock_down = request.getParameter("form2_stock_down");
        String form2_stock_up = request.getParameter("form2_stock_up");
        if ("form1".equals(form)) {  // 证明是固定预警
            String sql = "update item set stock_down=?,stock_up=? ,warning_value_status=1 where id= ? ";
            int result = db.doUpdate(sql, Integer.parseInt(form1_stock_down), Integer.parseInt(form1_stock_up), id);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("更新成功");
            } else {
                ud.setCode(1);
                ud.setMsg("更新失败");
            }
        } else if ("form2".equals(form)) {  // 证明是动态预警
            String sql = "update item set stock_down=200,stock_up=500 ,warning_value_status=2 where id= ? ";
//            int result = db.doUpdate(sql, Integer.parseInt(form2_stock_down), Integer.parseInt(form2_stock_up), id);
            int result = db.doUpdate(sql, id);

            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("更新成功");
            } else {
                ud.setCode(1);
                ud.setMsg("更新失败");
            }
        } else {
            ud.setCode(1);
            ud.setMsg("更新失败");
        }
        writeJson(ud, response);
    }


    // 更新库存|补货
    public void replenishment(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        System.out.println("replenishment");
        DataModel ud = new DataModel();
        String stock = request.getParameter("stock");
        String name = request.getParameter("name");
        if (stock == null || "".equals(stock)) {
            ud.setCode(1);
            ud.setMsg("请输入库存");
            writeJson(ud, response);
            return;
        }
        String sql = "update item set stock=? where name=?";
        int result = db.doUpdate(sql, Integer.parseInt(stock), name);
        if (result > 0) {
            ud.setCode(0);
            ud.setMsg("更新成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        writeJson(ud, response);
    }


    // 获取所有的缺货库存信息
    public void getAllLackStockData(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        System.out.println("getAllLackStockData");
        DataModel ud = new DataModel();
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        String totalSql = "select count(*) total from item where stock < warning_value";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select *  from item where stock < warning_value limit ? offset ?";
        List<Item> limitMaps = db.select(Item.class, limitSql, limit, skip);
        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
        } else {
            ud.setCode(1);
        }
        writeJson(ud, response);
    }


    // 设置预警值
    public void editWarning(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        System.out.println("editWarning");
        DataModel ud = new DataModel();
        String stock = request.getParameter("stock");
        String name = request.getParameter("name");
        String warning = request.getParameter("warning");
        if (warning == null || "".equals(warning)) {
            ud.setCode(1);
            ud.setMsg("请输入预警值");
            writeJson(ud, response);
            return;
        }
        String sql = "update item set warning_value=? where name=? and stock=?";
        int result = db.doUpdate(sql, Integer.parseInt(warning), name, stock);
        if (result > 0) {
            ud.setCode(0);
            ud.setMsg("更新成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        writeJson(ud, response);

    }


    // 1.获取所有的库存信息
    public void getAllStockData(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
//        System.out.println("getAllStockData");
        // 1.1 获取所有的库存信息
        DataModel ud = new DataModel();
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from item ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select *  from item limit ? offset ?";
        List<Item> limitMaps = db.select(Item.class, limitSql, limit, skip);
        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        writeJson(ud, response);
    }


}
