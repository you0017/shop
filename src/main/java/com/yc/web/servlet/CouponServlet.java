package com.yc.web.servlet;

import com.yc.bean.Coupon;
import com.yc.bean.Item;
import com.yc.bean.UserCoupon;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.YcConstants;
import com.yc.web.model.JsonModel;
import redis.clients.jedis.Jedis;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/html/coupon.action")
public class CouponServlet extends BaseServlet{

    private DBHelper db = new DBHelper();

    protected void checkCoupon(HttpServletRequest req,HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        String couponCode = req.getParameter("coupon_code");

        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        JsonModel jm = new JsonModel();

        //先看看这个优惠券是否存在
        List<Coupon> select = db.select(Coupon.class, "select * from coupons where code=? and status='active'",couponCode);
        if (select==null||select.size()<=0){
            jm.setCode(0);
            jm.setError("无效优惠券");
            writeJson(jm,resp);
            return;
        }

        //看看这个优惠券是否领取过
        List<UserCoupon> select1 = db.select(UserCoupon.class, "select * from user_coupon where user_id=? and coupon_id=?",user_id,select.get(0).getId());
        if (select1==null||select1.size()<=0){
            //没领过不能用
            jm.setCode(0);
            jm.setError("无效优惠券");
            writeJson(jm,resp);
            return;
        }

        //如果领了
        //看看这个优惠券是否使用过
        if (select1.get(0).getUsed()==1){
            jm.setCode(0);
            jm.setError("已经使用过了");
            writeJson(jm,resp);
            return;
        }
        if (select1.get(0).getUsed()==2){
            jm.setCode(0);
            jm.setError("已经过期过了");
            writeJson(jm,resp);
            return;
        }

        //判断这个优惠券的条件是否满足
        //商品id -- 数量
        Map<String, String> item_id = redis.hgetAll(YcConstants.CARTITEMS + user_id);
        String sql = "select * from item where id in (";
        Set<Map.Entry<String, String>> entries = item_id.entrySet();
        List list = new ArrayList();
        for (Map.Entry<String, String> entry : entries) {
            sql+="?,";
            list.add(entry.getKey());
        }
        sql = sql.substring(0,sql.length()-1) + ")";

        //这里面我只需要单价
        List<Item> select2 = db.select(Item.class, sql, list.toArray());
        Double sum = 0.0;
        for (Item item : select2) {
            String num = item_id.get(item.getId());
            sum+=item.getPrice()*Integer.valueOf(num);//这是总价
        }

        //目前我的优惠券格式是  >xxx
        Double v = Double.valueOf(select.get(0).getUsage_limit().substring(1));
        if (sum>v){
            //代表可用
            jm.setCode(1);
            jm.setObj(select.get(0));//优惠券传回去
        }else{
            //不可用
            jm.setCode(0);
            jm.setError("不满足优惠券条件");
        }

        writeJson(jm,resp);
    }

    protected void coupon(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        String coupon_id = req.getParameter("coupon_id");//优惠券id
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.close();
        JsonModel jm = new JsonModel();

        if (user_id==null){
            jm.setCode(0);
            jm.setError("请先登录");
            writeJson(jm,resp);
            return;
        }

        List<Coupon> select1 = db.select(Coupon.class, "select * from coupons where id=? and status='active' ", coupon_id);
        if (select1==null||select1.size()<=0){
            jm.setCode(0);
            jm.setError("该优惠券已过期");
            writeJson(jm,resp);
            return;
        }

        //这个优惠券存在

        //看看是否领过了
        List<UserCoupon> select = db.select(UserCoupon.class, "select * from user_coupon where user_id=? and coupon_id=?",user_id,coupon_id);//看看领过了没有
        if (select.size()>0){
            jm.setCode(0);
            jm.setError("已经领取过了");
            writeJson(jm,resp);
            return;
        }

        db.doUpdate("insert into user_coupon values(null,?,?,false)",user_id,coupon_id);

    }

    /**
     * 首页展示所有的优惠券
     * @param req
     * @param resp
     */
    protected void getCoupons(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.close();
        //拿到所有可展示的优惠券
        List<Coupon> select = db.select(Coupon.class, "select * from coupons where status='active'");

        if (user_id!=null){
            //所有的优惠券看看对应的用户是否领取过
            List<UserCoupon> select1 = db.select(UserCoupon.class, "select * from user_coupon where user_id=?", user_id);

            //遍历所有的可领取的优惠券
            for (Coupon coupon : select) {
                //看看用户领取了没有
                for (UserCoupon userCoupon : select1) {
                    //领过了就让前端变灰
                    if (coupon.getId().equals(userCoupon.getCoupon_id())){
                        coupon.setStatus("expired");
                    }
                }
            }
        }

        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);

    }
}
