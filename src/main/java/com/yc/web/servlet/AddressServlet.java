package com.yc.web.servlet;

import com.yc.bean.Address;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.YcConstants;
import com.yc.web.model.JsonModel;
import com.yc.web.service.AddressService;
import redis.clients.jedis.Jedis;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/html/address.action")
public class AddressServlet extends BaseServlet{
    private DBHelper db = new DBHelper();

    /**
     * 读取默认地址
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void getDefault(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Jedis redis = RedisHelper.getRedisInstance();
        JsonModel jm = new JsonModel();
        String userId = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        List<Address> select = db.select(Address.class, "select * from address where user_id=? and is_default=1", userId);
        if (select==null||select.size()<=0){
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }
        redis.close();
        jm.setCode(1);
        jm.setObj(select.get(0));
        writeJson(jm,resp);
    }

    /**
     * 修改地址
     */
    protected void modifyAddress(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        AddressService addressService = new AddressService();
        JsonModel jm = new JsonModel();
        if (redis.get(YcConstants.SHOP_USERID+req.getSession().getId())==null){
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }
        try {
            addressService.modifyAddress(req,resp);
        } catch (Exception e) {
            jm.setError(e.getMessage());
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }

        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 新增地址
     * @param req
     * @param resp
     */
    protected void addAddress(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        AddressService addressService = new AddressService();
        JsonModel jm = new JsonModel();
        if (redis.get(YcConstants.SHOP_USERID+req.getSession().getId())==null){
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }
        try {
            addressService.addAddress(req,resp);
        } catch (Exception e) {
            jm.setError(e.getMessage());
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }

        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 删除地址
     * @param req
     * @param resp
     */
    protected void deleteAddress(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        Jedis redis = RedisHelper.getRedisInstance();
        HttpSession session = req.getSession();
        String userId = redis.get(YcConstants.SHOP_USERID + session.getId());
        JsonModel jm = new JsonModel();

        if (userId==null||userId.equals("")||userId.length()==0){
            redis.close();
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }

        String sql = "delete from address where id=? and user_id=?";
        int i = db.doUpdate(sql, id, userId);
        if (i==0){
            redis.close();
            jm.setCode(0);
            jm.setError("删除失败");
            writeJson(jm,resp);
            return;
        }
        redis.close();
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 设置默认地址
     * @param req
     * @param resp
     * @throws Exception
     */
    protected void setDefault(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String id = req.getParameter("id");
        Jedis redis = RedisHelper.getRedisInstance();
        HttpSession session = req.getSession();
        String userId = redis.get(YcConstants.SHOP_USERID + session.getId());

        //查出用户所有的地址
        String sql = "select * from address where user_id=?";
        List<Address> select = db.select(Address.class, sql, userId);

        //太长了要用事务
        AddressService addressService = new AddressService();
        addressService.setDefault(select,id);
    }

    /**
     * 获取地址
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected void getAllAddress(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        HttpSession session = req.getSession();
        JsonModel jm = new JsonModel();
        String id = redis.get(YcConstants.SHOP_USERID + session.getId());//用户id

        if (id==null||id.equals("")||id.length()==0){
            redis.close();
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }

        String sql = "select * from address where user_id=?";
        List<Address> select = db.select(Address.class, sql, id);

        redis.close();
        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);
    }
}
