package com.yc.web.service;



import com.yc.bean.*;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.YcConstants;
import com.yc.web.model.JsonModel;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderService {
    private DBHelper dbHelper = new DBHelper();

    /**
     * 取消订单
     * @param reason
     * @param id
     */
    public void cancel(String reason, String id) throws SQLException, ClassNotFoundException {
        int result=0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        //以事务处理方式添加订单信息
        try{
            connection = dbHelper.getConnection();
            connection.setAutoCommit(false);//关闭隐式提交  防止完成一次就提交一次
            String sql = "update `order` set status=8 , reason=? where id=?";
            //后面那个常量表示自动生成的id返回
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setObject(1,reason);
            preparedStatement.setObject(2,id);

            preparedStatement.executeUpdate();

            sql = "select item_id from order_detail where order_id=?";

            //根据订单号查所有的商品id
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,id);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                //遍历所有商品id
                Object i = rs.getObject(1);
                sql = "update item set sold=sold-1 , stock=stock+1 where id=?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1,i);
                preparedStatement.executeUpdate();
            }


            //事务提交
            connection.commit();

        }catch (Exception e){
            if (connection!=null){
                connection.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("数据库异常，取消失败");
        }finally {
            if (connection!=null){
                connection.close();
            }
            if (preparedStatement!=null){
                preparedStatement.close();
            }
        }


        //return result;
    }

    /**
     * 订单详情问题
     * @param order 订单表
     * @param map   购物车的键值对
     * @param items 商品表
     * @return
     * @throws Exception
     */
    public int order(Order order, Map<String,String> map, List<Item> items) throws Exception {
        int result=0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        //以事务处理方式添加订单信息
        try{
            connection = dbHelper.getConnection();
            connection.setAutoCommit(false);//关闭隐式提交  防止完成一次就提交一次
            String sql = "insert into `order`(total_fee,user_id,status,create_time,update_time,address,contact,mobile,actual_payment,coupon_id,shipping_fee) values(?,?,?,?,?,?,?,?,?,?,?)";
            //后面那个常量表示自动生成的id返回
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setObject(1,order.getTotal_fee());
            preparedStatement.setObject(2,order.getUser_id());
            preparedStatement.setObject(3,order.getStatus());
            preparedStatement.setObject(4,LocalDateTime.now());
            preparedStatement.setObject(5,LocalDateTime.now());
            preparedStatement.setObject(6,order.getAddress());
            preparedStatement.setObject(7,order.getContact());
            preparedStatement.setObject(8,order.getMobile());
            preparedStatement.setObject(9,order.getActual_payment());
            preparedStatement.setObject(10,order.getCoupon_id());
            preparedStatement.setObject(11,order.getShipping_fee());

            //现在是order表已经存在
            preparedStatement.executeUpdate();

            int roid=0;
            //取自动生成的id
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()){
                roid = generatedKeys.getInt(1);
            }

            Double discount = 0.0;
            //用过券
            if (!order.getCoupon_id().equals(Integer.valueOf("0"))){
                List<Coupon> select = dbHelper.select(Coupon.class, "select * from coupons where id=?", order.getCoupon_id());
                discount = select.get(0).getDiscount();//得到折扣价格
            }

            //循环所有的订单项，添加到resorderItem表
            if (items == null || items.size()<=0){
                throw new RuntimeException("购物车为空");
            }

            sql = "insert into order_detail(order_id,item_id,num,name,spec,price,image,create_time,update_time,actual_payment) values(?,?,?,?,?,?,?,?,?,?)";
            String sql2="update item set stock = stock-1 ,sold = sold+1 ,update_time = ? where id=?";//这个是用来商品数量变化的
            for (Item item : items) {
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1,roid);    //订单id
                preparedStatement.setObject(2,item.getId());//商品id
                preparedStatement.setObject(3,map.get(item.getId()));//商品数量
                preparedStatement.setObject(4,item.getName());//商品名
                preparedStatement.setObject(5,item.getSpec());//商品描述
                preparedStatement.setObject(6,item.getPrice());//商品价格
                preparedStatement.setObject(7,item.getImage());//商品图片
                preparedStatement.setObject(8, LocalDateTime.now());//时间
                preparedStatement.setObject(9,LocalDateTime.now());
                if (!order.getCoupon_id().equals("0")){
                    //这是用过优惠券

                    // item.getPrice() / order.getTotal_fee() 保留两位小数的优化
                    Double v = Double.parseDouble(String.format("%.2f", item.getPrice() / order.getTotal_fee()));

                    preparedStatement.setObject(10,item.getPrice()+v*discount);//实际价格
                }else{
                    //没用过券
                    preparedStatement.setObject(10,item.getPrice());//实际价格
                }

                preparedStatement.executeUpdate();
                //数量变化
                preparedStatement = connection.prepareStatement(sql2);
                preparedStatement.setObject(1,LocalDateTime.now());
                preparedStatement.setObject(2,item.getId());
                preparedStatement.executeUpdate();
            }
            //事务提交
            connection.commit();
            result = roid;
        }catch (Exception e){
            if (connection!=null){
                connection.rollback();
            }
            throw e;
        }finally {
            if (connection!=null){
                connection.close();
            }
            if (preparedStatement!=null){
                preparedStatement.close();
            }
        }
        StringBuilder sb = new StringBuilder();


        return result;
    }


}
