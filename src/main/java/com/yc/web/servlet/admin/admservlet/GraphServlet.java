package com.yc.web.servlet.admin.admservlet;

import com.yc.bean.Item;
import com.yc.web.model.DataModel;
import com.yc.web.model.JsonModel;
import com.yc.web.servlet.admin.bean.AdminOrderGrap;
import com.yc.web.servlet.admin.bean.AdminTest;
import com.yc.web.servlet.admin.bean.AdminTop;
import com.yc.web.servlet.admin.bean.GraphSale;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/graph.action")
public class GraphServlet extends BaseServlet {

    // 订单的数据
public void getInitOrderGraph(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
    JsonModel jsonModel = new JsonModel();
    String sql = "SELECT DATE(create_time) AS order_time, COUNT(*) AS order_sum, SUM(total_fee) AS order_sales " +
            "FROM `order` WHERE create_time >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time)";
    ArrayList end = new ArrayList<>();
    List<AdminOrderGrap> select = db.select(AdminOrderGrap.class, sql);
    ArrayList<Integer>  order_sum = new ArrayList<>();   // 订单量
    ArrayList<Double> order_sales = new ArrayList<>();  // 销售额
    ArrayList<String> order_time = new ArrayList<>();  // 时间
    for (AdminOrderGrap adminOrderGrap : select) {
        order_time.add(adminOrderGrap.getOrder_time().toString());
        order_sum.add(adminOrderGrap.getOrder_sum().intValue());
        order_sales.add(adminOrderGrap.getOrder_sales());
    }
    end.add(order_time);
    end.add(order_sum);
    end.add(order_sales);
    jsonModel.setObj(end);
    jsonModel.setCode(1);
    jsonModel.setError("");
    writeJson(jsonModel, response);


}




    // 下面的商品销售的数据
    // 年度top
    public void yearTop(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String sql ="SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
                "GROUP BY i.id ORDER BY sold_quantity DESC";
        String sqlTotal = "SELECT COUNT(*) as total FROM ("+
                "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
                "GROUP BY i.id"
                + ") AS subquery";
        List<AdminTop> select = db.select(AdminTop.class, sql);
        List<Map<String, Object>> maps = db.select(sqlTotal);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        ud.setCode(0);
        ud.setData(select);
        ud.setCount(total);
        writeJson(ud, response);

    }

    // 本月
    public void monthTop(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String sql = "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
                "GROUP BY i.id ORDER BY sold_quantity DESC";
        String sqlTotal = "SELECT COUNT(*) as total FROM ("+
                "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE MONTH(od.create_time) = MONTH(CURRENT_DATE) AND YEAR(od.create_time) = YEAR(CURRENT_DATE) " +
                "GROUP BY i.id"
                + ") AS subquery";
        List<AdminTop> select = db.select(AdminTop.class, sql);
        List<Map<String, Object>> maps = db.select(sqlTotal);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        writeJson(ud, response);
    }

    // 昨日销量top
    public void yesterdayTop(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String sql = "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE DATE(od.create_time) = CURDATE() - 1 GROUP BY i.id ORDER BY sold_quantity DESC";
        String sqlTotal = "SELECT COUNT(*) as total FROM ("+
                "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE DATE(od.create_time) = CURDATE() - 1 GROUP BY i.id ORDER BY sold_quantity DESC"
                + ") AS subquery";
        List<Map<String, Object>> maps = db.select(sqlTotal);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        List<AdminTop> select = db.select(AdminTop.class, sql);
        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        writeJson(ud, response);
    }

    // 今日销量top
    public void getTodayTop(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String sql = "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE DATE(od.create_time) = CURDATE() GROUP BY i.id ORDER BY sold_quantity DESC";
        String sqlTotal = "SELECT COUNT(*) as total FROM ("+
                "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id " +
                "WHERE DATE(od.create_time) = CURDATE() GROUP BY i.id"
                + ") AS subquery";
        List<AdminTop> select = db.select(AdminTop.class, sql);
        List<Map<String, Object>> maps = db.select(sqlTotal);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        writeJson(ud, response);

    }

    // 销量前30
    public void getGoodsTop(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        String sql = "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                     "FROM item i JOIN order_detail od ON i.id = od.item_id GROUP BY i.id ORDER BY sold_quantity DESC limit ? offset ?";
        String sqlTotal = "SELECT COUNT(*) as total FROM ("+
                "SELECT i.name, SUM(od.num) AS sold_quantity, SUM(od.num * od.price) AS total_sales " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id GROUP BY i.id " +
                ") AS subquery";
        List<Map<String, Object>> maps = db.select(sqlTotal);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        List<AdminTop> select = db.select(AdminTop.class, sql, limit, skip);
        ud.setCode(0);
        ud.setCount(total);
        ud.setData(select);
        writeJson(ud, response);

    }

    // 指定年月的数据
    public void getMonthYearGraph(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonModel jm = new JsonModel();
        String month = request.getParameter("month");
        String year = request.getParameter("year");
        String sql = "SELECT item.category, SUM(order_detail.num * order_detail.price) AS total_revenue, SUM(order_detail.num) AS total_sales " +
                "FROM item INNER " +
                "JOIN order_detail ON item.id = order_detail.item_id INNER " +
                "JOIN `order` ON order_detail.order_id = `order`.id " +
                "WHERE YEAR(`order`.create_time) = ? AND MONTH(`order`.create_time) = ? GROUP BY item.category";
        List<GraphSale> select = db.select(GraphSale.class, sql, year, month);
        ArrayList end = new ArrayList<>();
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jm.setObj(end);
        jm.setCode(1);
        jm.setError("");
        writeJson(jm, response);
    }

    // 今年的数据
    public void getYearGraph(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonModel jsonModel = new JsonModel();
        String sql = "SELECT item.category, SUM(order_detail.num * order_detail.price) AS total_revenue, SUM(order_detail.num) AS total_sales " +
                "FROM item INNER JOIN order_detail ON item.id = order_detail.item_id INNER JOIN `order` ON order_detail.order_id = `order`.id " +
                "WHERE YEAR(`order`.create_time) = YEAR(CURRENT_DATE()) GROUP BY item.category";
        ArrayList end = new ArrayList<>();
        List<GraphSale> select = db.select(GraphSale.class, sql);
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
                        total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        writeJson(jsonModel, response);
    }

    // 这个月的数据
    public void getMonthGraph(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonModel jsonModel = new JsonModel();
        String sql = "SELECT item.category, SUM(order_detail.num) AS total_sales, SUM(order_detail.price * order_detail.num) AS total_revenue " +
                "FROM item JOIN order_detail " +
                "ON item.id = order_detail.item_id JOIN `order` " +
                "ON order_detail.order_id = `order`.id " +
                "WHERE MONTH(`order`.create_time) = MONTH(CURRENT_DATE) " +
                "AND YEAR(`order`.create_time) = YEAR(CURRENT_DATE) GROUP BY item.category";
        ArrayList end = new ArrayList<>();
        List<GraphSale> select = db.select(GraphSale.class, sql);
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        writeJson(jsonModel, response);
    }

    // 昨天的数据
    public void getYesterdayGraph(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonModel jsonModel = new JsonModel();
        String sql = "SELECT item.category, SUM(order_detail.num) AS total_sales, SUM(order_detail.price * order_detail.num) AS total_revenue " +
                "FROM item JOIN order_detail " +
                "ON item.id = order_detail.item_id JOIN `order` " +
                "ON order_detail.order_id = `order`.id " +
                "WHERE DATE(`order`.create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) GROUP BY item.category";
        ArrayList end = new ArrayList<>();
        List<GraphSale> select = db.select(GraphSale.class, sql);
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        writeJson(jsonModel, response);
    }


    // 获取今日销售情况
    public void getTodayGraph(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonModel jsonModel = new JsonModel();
        String sql = "SELECT item.category, SUM(order_detail.num) AS total_volume " +
                "FROM order_detail JOIN item ON order_detail.item_id = item.id " +
                "WHERE DATE(order_detail.create_time) = CURDATE() GROUP BY item.category";

        String sql2 = "SELECT i.category as category, SUM(od.num) AS total_sales, ROUND(SUM(od.num * od.actual_payment), 2) AS total_revenue " +
                "FROM order_detail od JOIN item i ON od.item_id = i.id " +
                "WHERE DATE(od.create_time) = CURDATE() GROUP BY i.category";
        ArrayList end = new ArrayList<>();
        List<GraphSale> select = db.select(GraphSale.class, sql2);
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue().doubleValue());
            total_sales.add(graph.getTotal_sales().intValue());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        writeJson(jsonModel, response);
    }

    // 初始化图表
    public void getInitGraph(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonModel jsonModel = new JsonModel();
//        String sql = "select category, SUM(sold) as sold from item GROUP BY category";
        String sql2 = "SELECT name, SUM(num) AS total_sales, ROUND(SUM(num * actual_payment), 1) AS total_revenue FROM order_detail GROUP BY name ORDER BY total_revenue ASC";
        String sql3 = "SELECT i.category as category, SUM(od.num) AS total_sales, ROUND(SUM(od.num * od.actual_payment), 2) AS total_revenue " +
                "FROM item i JOIN order_detail od ON i.id = od.item_id GROUP BY i.category";
        ArrayList end = new ArrayList<>();
        List<GraphSale> select = db.select(GraphSale.class, sql3);
        ArrayList<Double> total_revenue = new ArrayList<>();   // 销售总额
        ArrayList<Integer> total_sales = new ArrayList<>();  // 销售笔数
        ArrayList<String> name = new ArrayList<>();  // 商品名字
        for (GraphSale graph : select) {
            total_revenue.add(graph.getTotal_revenue());
            total_sales.add(graph.getTotal_sales());
            name.add(graph.getCategory());
        }
        end.add(name);
        end.add(total_sales);
        end.add(total_revenue);
        jsonModel.setObj(end);
        jsonModel.setCode(1);
        jsonModel.setError("");
        writeJson(jsonModel, response);
    }
}
