package com.yc.web.servlet.admin.admservlet;


import com.yc.bean.Order;
import com.yc.bean.ReturnOrder;
import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.web.model.DataModel;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// 订单的操作

@WebServlet("/admin/order.action")
public class OrderServlet extends BaseServlet{

    // 允许退货
    public void okView(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 查询订单表里的数据
        DataModel ud = new DataModel();
        String id = request.getParameter("id");
    }


    // 申请退货订单的数据
    public void getAllBackOrderData(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        // 查询订单表里的数据
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        String totalSql = "select count(*) total from returnorders ";
        String sql = "SELECT * FROM returnorders limit ? offset ?";
        List<ReturnOrder> list = db.select(ReturnOrder.class, sql, limit, skip);
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        ud.setCount(total);
        ud.setData(list);
        ud.setCode(0);
        writeJson(ud, response);
    }


    // 恢复已删除订单
    public void allRecover(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 订单表里改状态
        // 商品表里也要改数量  多表查询
        // 查出订单表里的详情数据  商品ID 购买数量
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("UPDATE `order` SET status = 4 WHERE ID IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i>=1){
            ud.setCode(0);
        }else {
            ud.setCode(1);
            ud.setMsg("恢复失败");
        }
        writeJson(ud, response);
    }

    // 批量发货
    public void multiSend(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        String sendCompany = request.getParameter("send_company");
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = currentTime.format(formatter);  // 发货时间
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        // 生成快递单
        Random random = new Random();
        String seng_id = String.valueOf(random.nextInt(300000));   // 商家生成的快递单号
        String sqlAllOrder = "SELECT * FROM `order` WHERE status = 2 and ID IN ( ";
        // 更新状态
        StringBuffer sql = new StringBuffer("UPDATE `order` SET status = 3  , " +
                "send_company='"+sendCompany +"', consign_time='"+formattedTime +"' WHERE ID IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
            sqlAllOrder += "?,";
        }
        sql.append(" ?)");
        sqlAllOrder += "?)";
        List<Order> select = db.select(Order.class, sqlAllOrder, intArray);
        for (Order order : select) {
            String logisticsSql = "insert into logistics (order_id, shipping_status, receiver_name, receiver_phone, " +
                    "receiver_address, logistics_company, tracking_number, remarks)" +
                    " values (?, ?, ?, ?, ?, ?, ?, ?)";
            int temp = db.doUpdate(logisticsSql, order.getId(), 1, order.getContact(),
                    order.getMobile(), order.getAddress(), order.getSend_company(), seng_id, "无备注");
            if (temp < 1) {
                ud.setCode(1);
                ud.setMsg("物流订单生成失败");
                writeJson(ud, response);
                return;
            }
        }
        int i = db.doUpdate(sql.toString(), intArray);
        // 更新时间和公司
        if (i >= 1 ) {
           ud.setCode(0);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 全部发货
    public void allSend(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String sqlAllOrder = "SELECT * FROM `order` WHERE status = 2";
        // 所有代发货订单
        List<Order> list = db.select(Order.class, sqlAllOrder);
        Random random = new Random();
        for (Order order : list) {
            String seng_id = String.valueOf(random.nextInt(300000));   // 商家生成的快递单号
            String logisticsSql = "insert into logistics (order_id, shipping_status, receiver_name, receiver_phone, " +
                    "receiver_address, logistics_company, tracking_number, remarks)" +
                    " values (?, ?, ?, ?, ?, ?, ?, ?)";
            int temp = db.doUpdate(logisticsSql, order.getId(), 1, order.getContact(),
                    order.getMobile(), order.getAddress(), order.getSend_company(), seng_id, "无备注");
            if (temp < 1) {
                ud.setCode(1);
                ud.setMsg("物流订单生成失败");
                writeJson(ud, response);
                return;
            }
        }
        String sendCompany = request.getParameter("send_company");
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = currentTime.format(formatter);  // 发货时间
        StringBuffer sql = new StringBuffer("UPDATE `order` SET status = 3, " +
                "send_company='"+sendCompany +"', consign_time='"+formattedTime +"' WHERE status=2");
        int i = db.doUpdate(sql.toString());
        if (i >= 1 ) {
            ud.setCode(0);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }
    // 发货功能
    public void recover(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 订单表里改状态
        // 发货表里面
        DataModel ud = new DataModel();
        String userId = request.getParameter("user_id");  // 用户id
        String order_id = request.getParameter("order_id");   // 订单号
        String userName = request.getParameter("user_name");  // 用户名
        String orderAddress = request.getParameter("order_address");  // 收货地址
        String mobile = request.getParameter("mobile");   // 收货电话
        String sendCompany = request.getParameter("send_company");   // 商家选的快递公司
        String remark = request.getParameter("remark");  // 商家备注
        Random random = new Random();
        String seng_id = String.valueOf(random.nextInt(300000));   // 商家生成的快递单号
        // 生成物流订单
        String logisticsSql ="insert into logistics (order_id, shipping_status, receiver_name, receiver_phone, " +
                     "receiver_address, logistics_company, tracking_number, remarks)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?)";
        int logisticsResult = db.doUpdate(logisticsSql, order_id, 1, userName, mobile, orderAddress, sendCompany, seng_id, remark);
        // 如果物流订单生成失败
       if ( logisticsResult<1){
           ud.setCode(1);
           ud.setMsg("物流订单生成失败");
           writeJson(ud, response);
           return;
       }

       // 修改交易订单状态
        String sql = "update `order` set status = 3, send_id=?, send_company=? where id =?";
        int i = db.doUpdate(sql, seng_id, sendCompany, order_id);
        if (i>=1){
            ud.setCode(0);
            ud.setMsg("发货成功");
        }else {
            ud.setCode(1);
            ud.setMsg("发货失败");
        }
        writeJson(ud, response);
    }

    // 模糊查询
    public void fuzzyQuery(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String id = request.getParameter("id");// 账号
        String status = request.getParameter("status");// 状态

        String sql = "select * from `order` where 1=1";
        List<Object> params = new ArrayList<>();
        if (id != null && id.trim().isEmpty() == false) {
            id = id.trim();
            params.add(id);
            sql += " and user_id = ? ";
        }
        if (status != null && status.trim().isEmpty() == false) {
            status = status.trim();
            params.add(status);
            sql += " and status = ? ";
        }else {
            sql += " and status <=4 ";
        }

        if ( startTime!=null && !"".equals(startTime) &&  endTime!=null && !"".equals(endTime)  ){  // 都有
            startTime = startTime.trim();
            endTime = endTime.trim();
            params.add(startTime);
            params.add(endTime);
            sql += " and create_time BETWEEN  ? and ?";
        }else if (startTime!=null && !"".equals(startTime)){ // 有开始
            startTime = startTime.trim();
            params.add(startTime);
            sql += " and create_time > ?";
        }else if ( endTime!=null && !"".equals(endTime) ){ // 有结束
            endTime = endTime.trim();
            params.add(endTime);
            sql += " and create_time < ?";
        }else if ( startTime==null && "".equals(startTime) && endTime==null && "".equals(endTime)){
            sql += "";
        }
        List<Order> select = db.select(Order.class, sql, params.toArray());
        int total = select.size();
        if ( select!=null && select.size()>0 ) {
            ud.setData(select);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        writeJson(ud, response);
    }

    // 单个删除
    public void del(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        StringBuffer sql = new StringBuffer("update `order` set status=5 where id = ? ");
        int i = db.doUpdate(sql.toString(), idsStr);
        if (i >= 1) {
            ud = allOrderData(ud, request);
            ud.setCode(0);
            ud.setMsg("删除成功");
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    //  批量删除 不做删除    状态码变为7
    public void batchDel(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("UPDATE `order` SET status = 5 WHERE ID IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i >= 1) {
            ud = allOrderData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 获取所有订单
    public void getAllOrderData(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
//        ud = UserData.allUserData(ud, request);
        ud = allOrderData(ud, request);
        writeJson(ud, response);
    }

    // 获取所有用户订单的方法
    private static DataModel allOrderData (DataModel ud, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from `order` ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString()) ;
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select * from `order` where status <=4 limit "+limit+" offset " + skip;
        List<Order> limitMaps = db.select(Order.class, limitSql);
        for (Order limitMap : limitMaps) {
            if ( limitMap.getCreate_time()!=null && !"".equals(limitMap.getCreate_time())){
                limitMap.setCreate_time(limitMap.getCreate_time().substring(0, limitMap.getCreate_time().length()-2));
            }
            if ( limitMap.getPay_time()!=null && !"".equals(limitMap.getPay_time())){
                limitMap.setPay_time(limitMap.getPay_time().substring(0, limitMap.getPay_time().length()-2));
            }
            if ( limitMap.getEnd_time()!=null && !"".equals(limitMap.getEnd_time())){
                limitMap.setEnd_time(limitMap.getEnd_time().substring(0, limitMap.getEnd_time().length()-2));
            }
            if ( limitMap.getConsign_time()!=null && !"".equals(limitMap.getConsign_time())){
                limitMap.setConsign_time(limitMap.getConsign_time().substring(0, limitMap.getConsign_time().length()-2));
            }
        }

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


    // 获取所有未发货订单
    public void getAllUnshippedOrderData(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DBHelper db = new DBHelper();
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from `order` ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString()) ;
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select * from `order` where status =2 limit "+limit+" offset " + skip;
        List<Order> limitMaps = db.select(Order.class, limitSql);
        for (Order limitMap : limitMaps) {
            if ( limitMap.getCreate_time()!=null && !"".equals(limitMap.getCreate_time())){
                limitMap.setCreate_time(limitMap.getCreate_time().substring(0, limitMap.getCreate_time().length()-2));
            }
            if ( limitMap.getPay_time()!=null && !"".equals(limitMap.getPay_time())){
                limitMap.setPay_time(limitMap.getPay_time().substring(0, limitMap.getPay_time().length()-2));
            }
            if ( limitMap.getEnd_time()!=null && !"".equals(limitMap.getEnd_time())){
                limitMap.setEnd_time(limitMap.getEnd_time().substring(0, limitMap.getEnd_time().length()-2));
            }
            if ( limitMap.getConsign_time()!=null && !"".equals(limitMap.getConsign_time())){
                limitMap.setConsign_time(limitMap.getConsign_time().substring(0, limitMap.getConsign_time().length()-2));
            }
        }

        if ( limitMaps!=null && limitMaps.size()>0 ) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        writeJson(ud, response);
    }

    //
    public void getAllrRmovedOorderData(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from `order` ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString()) ;
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select * from `order` where status =5 limit "+limit+" offset " + skip;
        List<Order> limitMaps = db.select(Order.class, limitSql);
        for (Order limitMap : limitMaps) {
            if ( limitMap.getCreate_time()!=null && !"".equals(limitMap.getCreate_time())){
                limitMap.setCreate_time(limitMap.getCreate_time().substring(0, limitMap.getCreate_time().length()-2));
            }
            if ( limitMap.getPay_time()!=null && !"".equals(limitMap.getPay_time())){
                limitMap.setPay_time(limitMap.getPay_time().substring(0, limitMap.getPay_time().length()-2));
            }
            if ( limitMap.getEnd_time()!=null && !"".equals(limitMap.getEnd_time())){
                limitMap.setEnd_time(limitMap.getEnd_time().substring(0, limitMap.getEnd_time().length()-2));
            }
            if ( limitMap.getConsign_time()!=null && !"".equals(limitMap.getConsign_time())){
                limitMap.setConsign_time(limitMap.getConsign_time().substring(0, limitMap.getConsign_time().length()-2));
            }
        }

        if ( limitMaps!=null && limitMaps.size()>0 ) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        writeJson(ud, response);
    }

}
