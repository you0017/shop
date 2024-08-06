package com.yc.web.servlet;

import com.yc.bean.*;
import com.yc.config.AlipayConfig;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.YcConstants;
import com.yc.web.model.JsonModel;
import com.yc.web.service.OrderService;
import redis.clients.jedis.Jedis;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/html/order.action")
public class OrderServlet extends BaseServlet{
    private DBHelper db = new DBHelper();


    /**
     * 获取退货单详情
     * @param req
     * @param resp
     */
    protected void getReturnOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.close();
        JsonModel jm = new JsonModel();
        if (user_id==null){
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }

        List<ReturnOrder> select = db.select(ReturnOrder.class, "select * from returnorders where customer_id=? order by return_id desc", user_id);

        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);


    }

    /**
     * 退货
     * @param req
     * @param resp
     */
    protected void ret(HttpServletRequest req,HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        String op = req.getParameter("index");//操作
        String item_id = req.getParameter("item_id");//商品id
        String order_id = req.getParameter("order_id");//订单id
        String reason = req.getParameter("reason");//退货原因


        String trackingCompany = req.getParameter("tracking_company");//公司
        String trackingNumber = req.getParameter("tracking_number");//快递单号
        String return_id = req.getParameter("return_id");//退货单id
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
        int i=0;
        if(op.equals("1")){//退货
            //查订单的相关详细订单
            List<OrderDetail> select = db.select(OrderDetail.class, "select * from order_detail where order_id=? and item_id=?", order_id, item_id);

             i = db.doUpdate("insert into returnorders(order_id,customer_id,product_id,return_reason,return_quantity" +
                            ",return_status,return_date,refund_amount,product_name,back_type) " +
                            "values(?,?,?,?,?,?,now(),?,?,2)", order_id, user_id, item_id, reason, select.get(0).getNum(),
                    1, select.get(0).getActual_payment() * select.get(0).getNum(), select.get(0).getName());

            db.doUpdate("update order_detail set return_status=2 where order_id=? and item_id=?", order_id, item_id);
        }else if (op.equals("3")){//仅退款
            //查订单的相关详细订单
            List<OrderDetail> select = db.select(OrderDetail.class, "select * from order_detail where order_id=? and item_id=?", order_id, item_id);

            i = db.doUpdate("insert into returnorders(order_id,customer_id,product_id,return_reason,return_quantity" +
                            ",return_status,return_date,refund_amount,product_name,back_type) " +
                            "values(?,?,?,?,?,?,now(),?,?,1)", order_id, user_id, item_id, reason, select.get(0).getNum(),
                    1, select.get(0).getActual_payment() * select.get(0).getNum(), select.get(0).getName());

            db.doUpdate("update order_detail set return_status=2 where order_id=? and item_id=?", order_id, item_id);
        }else if (op.equals("2")){//退货填单号和公司，这是已经同意了
            i = db.doUpdate("update returnorders set tracking_number=?,tracking_company=?,return_status=3 where return_id=?",trackingNumber,trackingCompany,return_id);
        }
        //return_status 1申请 2申请通过 3快递审核 4通过 5不通过
        if (i>0){
            jm.setCode(1);
            jm.setObj("申请成功");
        }else{
            jm.setCode(0);
            jm.setError("未知异常");
        }
        writeJson(jm,resp);
    }
    /**
     * 取消订单
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void cancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String reason = req.getParameter("reason");//取消原因
        String id = req.getParameter("item_id");//订单号

        JsonModel jm = new JsonModel();
        OrderService orderService = new OrderService();
        try {
            orderService.cancel(reason,id);
        } catch (Exception e) {
            jm.setCode(0);
            jm.setError(e.getMessage());
            writeJson(jm,resp);
            return;
        }
        jm.setCode(1);
        writeJson(jm,resp);
    }
    /**
     * 确认收货
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        db.doUpdate("update `order` set status=?,end_time=?,update_time=? where id=?",4,LocalDateTime.now(),LocalDateTime.now(),id);
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 获取所有订单
     * @param req
     * @param resp
     */
    protected void getOrder(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        JsonModel jm = new JsonModel();
        if (user_id==null){
            jm.setCode(0);
            jm.setError("未登录");
            return;
        }

        int pageno = Integer.valueOf(req.getParameter("pageno"));
        int pagesize = Integer.valueOf(req.getParameter("pagesize"));

        int start = (pageno-1)*pagesize;
        //该用户所有订单
        String sql = "select id,total_fee,status,create_time,address,mobile,contact,actual_payment,shipping_fee from `order` where user_id=? and status != 7 order by id desc limit "+pagesize+" offset "+start;

        List<OrderList> list = db.select(OrderList.class, sql, user_id);
        for (OrderList orderList : list) {
            orderList.setCreate_time(orderList.getCreate_time().substring(0,orderList.getCreate_time().length()-2));
        }

        sql = "select * from order_detail where order_id=?";

        //每个订单对应什么商品
        for (OrderList order : list) {
            List<OrderDetail> select = db.select(OrderDetail.class, sql, order.getId());
            order.setOrderDetailList(select);
        }

        redis.close();
        jm.setCode(1);
        jm.setObj(list);
        writeJson(jm,resp);
    }

    /**
     * 支付
     * @param req
     * @param resp
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected void pay(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        String province = req.getParameter("province");
        String city = req.getParameter("city");
        String town = req.getParameter("town");
        String street = req.getParameter("street");
        String contact = req.getParameter("contact");
        String mobile = req.getParameter("mobile");
        String freight = req.getParameter("freight");

        JsonModel jm = new JsonModel();
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        //购物车 id <-> 数量
        Map<String, String> map = redis.hgetAll(YcConstants.CARTITEMS + user_id);

        if (user_id==null){
            jm.setCode(0);
            jm.setError("未登录");
            return;
        }

        /**
         * 目前是有了购物车的id及其数量，现在要遍历每个id进行查询，记录一下哪些id能够被买，如果stock<=0就不能买了
         * 记录id是方便在购物车中删除
         * 还有就是如果购物车中存在商品数量不足，就抛异常，不要继续执行，提醒前端xx商品不够了，要移除购物车才能进行支付
         * 还有就是考虑下架问题
         */
        String sql = "select * from item where id=?";
        Set<Map.Entry<String, String>> entries = map.entrySet();
        Double total = 0.0;
        List<Item> items = new ArrayList<>();

        for (Map.Entry<String, String> entry : entries) {
            List<Item> select = db.select(Item.class, sql, entry.getKey());
            if (select==null||select.size()<=0){
                jm.setCode(0);
                jm.setError("未知异常");
                writeJson(jm,resp);
                return;
            }
            //取出来判断库存和是否下架
            if (select.get(0).getStock()<=0 || select.get(0).getStatus()==0){
                jm.setCode(0);
                jm.setError(select.get(0).getName()+"库存不足或已下架");
                writeJson(jm,resp);
                return;
            }

            //总价
            total += select.get(0).getPrice() * Double.parseDouble(entry.getValue());
            items.add(select.get(0));
        }



        Order order = new Order();
        order.setTotal_fee(total); //原价
        order.setUser_id(Integer.valueOf(user_id));
        order.setStatus(1);//未支付
        //order.setCreate_time(LocalDateTime.now().toString());
        //order.setUpdate_time(LocalDateTime.now().toString());
        order.setAddress(province+city+town+street);
        order.setContact(contact);
        order.setMobile(mobile);
        order.setShipping_fee(Double.valueOf(freight));
        order.setCoupon_id(0);//券id  默认为0  insert方便一点，懒得判断了

        String coupon_id = req.getParameter("coupon_id");
        if (coupon_id!=null){
            //说明用了优惠券
            List<Coupon> select = db.select(Coupon.class, "select * from coupons where id=? and status='active'", coupon_id);
            Double discount = select.get(0).getDiscount();
            total = total + discount;
            order.setCoupon_id(Integer.valueOf(coupon_id));//券id
        }
        order.setActual_payment(total);//折后价格

        OrderService orderService = new OrderService();
        Integer order_id = null;
        try {                           //订单，购物车，和需要的商品
            order_id = orderService.order(order, map, items);//返回订单的id
        } catch (Exception e) {
            e.printStackTrace();
            jm.setCode(0);
            jm.setError(e.getMessage());
            writeJson(jm,resp);
            return;
        }
        order.setId(order_id);//订单id

        //并且把用的券改状态如果用了
        if (coupon_id!=null&&!order.getCoupon_id().equals(0)){
            db.doUpdate("update user_coupon set used=1 where user_id=? and coupon_id=?",user_id,coupon_id);
        }

        //删除购物车
        redis.del(YcConstants.CARTITEMS+user_id);
        jm.setCode(1);
        jm.setObj(order);
        writeJson(jm,resp);

        /**
         * 修改沙箱支付return地址
         */
        String protocol = req.isSecure() ? "https" : "http";
        String host = req.getServerName();
        int port = req.getServerPort();
        String contextPath = req.getContextPath();

        AlipayConfig.notify_url = protocol + "://" + host + ":" + port + contextPath + "/html/index.html?status=1";
        AlipayConfig.return_url = protocol + "://" + host + ":" + port + contextPath + "/html/index.html?status=1";
    }
}
