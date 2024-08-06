package com.yc.web.servlet.admin.admservlet;


import com.yc.bean.Order;
import com.yc.dao.DBHelper;
import com.yc.web.model.DataModel;
import com.yc.web.servlet.admin.bean.Sale;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/finance.action")
public class FinanceServlet extends BaseServlet {

    // 按时间段查询销售
// 年份来算    SELECT item.name, YEAR(`order`.end_time) AS year, SUM(order_detail.num) AS total_sales FROM order_detail JOIN `order` ON order_detail.order_id = `order`.id JOIN item ON order_detail.item_id = item.id WHERE `order`.end_time IS NOT NULL GROUP BY item.id, YEAR(`order`.end_time);
// SELECT YEAR(`order`.end_time) AS year, item.name, item.price, SUM(order_detail.num) AS total_sales, SUM(order_detail.num * order_detail.price) AS total_revenue FROM order_detail JOIN `order` ON order_detail.order_id = `order`.id JOIN item ON order_detail.item_id = item.id WHERE `order`.end_time IS NOT NULL GROUP BY YEAR(`order`.end_time), item.id;
    public void getSaleDataByTime(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String timeYear = request.getParameter("timeYear");
        String timeMonth = request.getParameter("timeMonth");
        String timeDay = request.getParameter("timeDay");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        // 查询指定时间段的数据
        DataModel ud = new DataModel();
        if ( timeYear!=null){
            String sql = "SELECT item.name, YEAR(`order`.end_time) AS year, " +
                    "SUM(order_detail.num) AS total_sales FROM order_detail " +
                    "JOIN `order` ON order_detail.order_id = `order`.id " +
                    "JOIN item ON order_detail.item_id = item.id " +
                    "WHERE `order`.end_time IS NOT NULL GROUP BY item.id, YEAR(`order`.end_time)";
        }
//        try {
//            ud = getAllrSaleDataByTime(ud, startTime, endTime);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
       writeJson(ud, response);
    }

    // 计算总的销售   不分时间段
    public void getSaleData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        try {
            ud = getAllrSaleData(ud, request);
        }catch (Exception e) {
            e.printStackTrace();
        }
       writeJson(ud, response);
    }

    public static DataModel getAllrSaleData( DataModel ud, HttpServletRequest request) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from item ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString()) ;
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        // SELECT `name`, price, price * sold AS total_price, sold  FROM item;
        String limitSql = "SELECT `name`, price, price * sold AS total_price, sold  FROM item limit ? offset ?";
        List<Sale> limitMaps = db.select(Sale.class, limitSql, limit, skip);
        if ( limitMaps!=null && limitMaps.size()>0 ) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        return ud;
    }

}
