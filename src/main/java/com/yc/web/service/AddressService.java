package com.yc.web.service;

import com.yc.bean.Address;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.YcConstants;
import com.yc.web.model.JsonModel;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressService {

    private DBHelper db = new DBHelper();

    public void modifyAddress(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String id1 = req.getParameter("id");
        String province = req.getParameter("province");
        String city = req.getParameter("city");
        String town = req.getParameter("town");
        String street = req.getParameter("street");
        String contact = req.getParameter("contact");
        String mobile = req.getParameter("mobile");
        String isDefault = req.getParameter("isDefault");

        String sql = "update address set ";
        List list = new ArrayList();
        if (province!=null&&province.trim().isEmpty() == false){
            sql += "province=?,";
            list.add(province);
        }
        if (city!=null&&city.trim().isEmpty() == false){
            sql += "city=?,";
            list.add(city);
        }
        if (town!=null&&town.trim().isEmpty() == false){
            sql += "town=?,";
            list.add(town);
        }
        if (street!=null&&street.trim().isEmpty() == false){
            sql += "street=?,";
            list.add(street);
        }
        if (contact!=null&&contact.trim().isEmpty() == false){
            sql += "contact=?,";
            list.add(contact);
        }
        if (mobile!=null&&mobile.trim().isEmpty() == false){
            sql += "mobile=?,";
            list.add(mobile);
        }
        if (isDefault.equals("1")){
            sql += "is_default=?,";
            list.add(isDefault);
        }

        sql = sql.substring(0,sql.length()-1)+" where id=?";
        list.add(id1);

        db.doUpdate(sql,list.toArray());
        Jedis redis = RedisHelper.getRedisInstance();
        String userid = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        List<Address> select = db.select(Address.class, "select * from address where user_id = ?", userid);
        //设置默认地址
        setDefault(select,id1);
    }

    public void addAddress(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ClassNotFoundException {
        String province = req.getParameter("province");
        String city = req.getParameter("city");
        String town = req.getParameter("town");
        String street = req.getParameter("street");
        String contact = req.getParameter("contact");
        String mobile = req.getParameter("mobile");
        String isDefault = req.getParameter("isDefault");
        Jedis redis = RedisHelper.getRedisInstance();
        String userId = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.close();
        JsonModel jm = new JsonModel();

        String sql1 = "insert into address(";
        String sql2 = "values(";
        List list = new ArrayList();
        if (userId!=null&&userId.trim().isEmpty() == false){
            sql1 += "user_id,";
            sql2 += "?,";
            list.add(userId);
        }
        if (province!=null&&province.trim().isEmpty() == false){
            sql1 += "province,";
            sql2 += "?,";
            list.add(province);
        }
        if (city!=null&&city.trim().isEmpty() == false){
            sql1 += "city,";
            sql2 += "?,";
            list.add(city);
        }
        if (town!=null&&town.trim().isEmpty() == false){
            sql1 += "town,";
            sql2 += "?,";
            list.add(town);
        }
        if (street!=null&&street.trim().isEmpty() == false){
            sql1 += "street,";
            sql2 += "?,";
            list.add(street);
        }
        if (contact!=null&&contact.trim().isEmpty() == false){
            sql1 += "contact,";
            sql2 += "?,";
            list.add(contact);
        }
        if (mobile!=null&&mobile.trim().isEmpty() == false){
            sql1 += "mobile,";
            sql2 += "?,";
            list.add(mobile);
        }
        if (isDefault!=null&&isDefault.trim().isEmpty() == false){
            sql1 += "is_default,";
            sql2 += "?,";
            list.add(isDefault);
        }

        sql1 = sql1.substring(0,sql1.length()-1)+") ";
        sql2 = sql2.substring(0,sql2.length()-1)+") ";

        String sql = sql1+sql2;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        //以事务处理方式添加订单信息
        try{
            connection = db.getConnection();
            connection.setAutoCommit(false);//关闭隐式提交  防止完成一次就提交一次

            //预编译
            preparedStatement = connection.prepareStatement(sql);

            //根据参数个数进行占位替代
            for (int i1 = 0; i1 < list.size(); i1++) {
                preparedStatement.setObject(i1+1,list.get(i1));
            }
            //这是新增地址
            preparedStatement.executeUpdate();


            //添加成功后这个是默认地址的话，其他要设置为非默认
            if(isDefault.equals("1")){
                List<Address> select = db.select(Address.class, "select * from address where user_id=?", userId);
                //新添加的地址一定是最后一个位置
                Integer id = 0;
                if (select.size()>0){
                    //不是第一个地址
                    id = select.get(select.size() - 1).getId();
                }
                //设置默认地址
                setDefault(select,id);
            }

            //事务提交
            connection.commit();
        }catch (Exception e){
            if (connection!=null){
                connection.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("添加失败");
        }finally {
            if (connection!=null){
                connection.close();
            }
            if (preparedStatement!=null){
                preparedStatement.close();
            }
        }

/*        int i = db.doUpdate(sql, list.toArray());
        if (i<=0){
            jm.setCode(0);
            jm.setError("添加失败");
            writeJson(jm,resp);
            return;
        }*/

    }

    /**
     * 设置默认地址
     * @param list  所有的该用户的地址
     * @param id    要设置为默认地址的id
     * @return
     * @throws Exception
     */
    public int setDefault(List<Address> list, Object id) throws Exception {
        int result=0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        //以事务处理方式添加订单信息
        try{
            connection = db.getConnection();
            connection.setAutoCommit(false);//关闭隐式提交  防止完成一次就提交一次

            String sql = "update address set is_default=? where id=?";
            preparedStatement = connection.prepareStatement(sql);
            for (Address address : list) {
                if (id.equals(address.getId().toString())){
                    preparedStatement.setObject(1,1);
                    preparedStatement.setObject(2,address.getId());
                }else{
                    preparedStatement.setObject(1,0);
                    preparedStatement.setObject(2,address.getId());
                }
                preparedStatement.executeUpdate();
            }
            //事务提交
            connection.commit();
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
