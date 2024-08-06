package com.yc.web.servlet;

import com.google.gson.Gson;
import com.yc.bean.Cart;
import com.yc.bean.Item;
import com.yc.bean.ItemPic;
import com.yc.bean.PageBean;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.YcConstants;
import com.yc.bean.CartItem;
import com.yc.web.model.JsonModel;
import com.yc.web.service.PageService;
import redis.clients.jedis.Jedis;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/html/item.action")
public class ItemServlet extends BaseServlet {
    private DBHelper db = new DBHelper();

    /**
     * 热门产品  用redis
     * @param req
     * @param resp
     */
    protected void getHot(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String hot = redis.get("hot");
        redis.close();
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(hot);
        writeJson(jm,resp);
    }

    /**
     * 联想查询
     * @param req
     * @param resp
     */
    protected void association(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        String association = req.getParameter("association");
        String sql = "select distinct name from (select * from item where (category like '%" + association + "%' or name like '%" + association + "%' or brand like '%" + association + "%' or spec like '%" + association + "%' or item_details like '%"+association+"%') and status=1 order by sold asc) a limit 6 offset 0";
        List<Map<String, Object>> select = db.select(sql);
        List list = new ArrayList();
        for (Map<String, Object> stringObjectMap : select) {
            list.add(stringObjectMap.get("name").toString());
        }
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(list);
        writeJson(jm,resp);
    }

    /**
     * 获取所有分类
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected void getCategories(HttpServletRequest req,HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        List<Item> select = db.select(Item.class, "select distinct category from item");
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);
    }

    /**
     * 根据分类查品牌
     * @param req
     * @param resp
     */
    protected void getBrandByCategories(HttpServletRequest req,HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        String category = req.getParameter("category");
        String sql = "select distinct brand from item where category like '%" + category + "%'";
        List<Item> select = db.select(Item.class, sql);
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);
    }
    /**
     * 获取所有品牌
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected void getBrands(HttpServletRequest req,HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        List<Item> select = db.select(Item.class, "select distinct brand from item");
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);
    }

    /**
     * 根据id删记录
     * @param req
     * @param resp
     */
    protected void delHistoryById(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        JsonModel jm = new JsonModel();
        if (user_id==null){
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }
        long zrem = redis.zrem(YcConstants.HISTORY + user_id, id);
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 清空历史记录
     * @param req
     * @param resp
     */
    protected void clearHistory(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID+req.getSession().getId());
        JsonModel jm = new JsonModel();
        if (user_id==null){
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }
        redis.del(YcConstants.HISTORY+user_id);
        redis.close();
        jm.setCode(1);
        writeJson(jm,resp);
    }
    /**
     * 获取浏览历史
     * @param req
     * @param resp
     */
    protected void getHistory(HttpServletRequest req,HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Jedis redis = RedisHelper.getRedisInstance();
        JsonModel jm = new JsonModel();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        if (user_id==null){
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }
        List<String> zrevrange = redis.zrevrange(YcConstants.HISTORY + user_id, 0, 2);
        if (zrevrange==null){
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }
        String sql = "select * from item where id in (";
        List list = new ArrayList<>();
        if (zrevrange==null||zrevrange.size()<=0){
            jm.setCode(1);
            jm.setObj(list);
            writeJson(jm,resp);
            return;
        }
        for (String s : zrevrange) {
            sql += "?,";
            list.add(s);
        }
        sql = sql.substring(0,sql.length()-1)+")";

        List<Item> select = db.select(Item.class, sql, list.toArray());
        list.clear();
        for (String s : zrevrange) {
            for (Item item : select) {
                if (item.getId().toString().equals(s)){
                    list.add(item);
                }
            }
        }

        jm.setCode(1);
        jm.setObj(list);
        writeJson(jm,resp);
        redis.close();
    }

    /**
     * 根据id移除购物车商品
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void removeItemById(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        JsonModel jm = new JsonModel();
        HttpSession session = req.getSession();
        //先获取用户id
        String id = redis.get(YcConstants.SHOP_USERID + session.getId());
        if (id==null||id.equals("")||id.length()<=0){
            jm.setCode(0);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }

        String itemId = req.getParameter("id");    //商品id
        //购物车key
        String key = YcConstants.CARTITEMS+id;
        redis.hdel(key,itemId);//删除这个键值对

        jm.setCode(1);
        writeJson(jm,resp);


    }

    /**
     * 根据id查商品detail
     * @param req
     * @param resp
     */
    protected void getItemById(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        String id = req.getParameter("id");
        String session_id = req.getSession().getId();
        //登录用户的历史浏览记录，足迹
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + session_id);//用户id
        if (user_id!=null){
            //存储格式是  历史记录_用户id: 访问时间戳 :商品id
            //              key     :   score   :value
            //用sorted set
            /*不重复但是有一个用于排序的隐含列
            zadd key score1 value1 score2 value2 ...  score权重，用来排序的*/
            redis.zadd(YcConstants.HISTORY+user_id,System.currentTimeMillis(),id);
        }
        redis.close();

        List<Item> select = db.select(Item.class, "select * from item where id=?", id);
        List<ItemPic> itemPics = db.select(ItemPic.class, "select * from itempic where itemid=?", id);
        Item item = select.get(0);
        item.setItempic(itemPics);
        JsonModel jm = new JsonModel();

        jm.setCode(1);
        jm.setObj(item);
        writeJson(jm,resp);

    }

    /**
     * 获取购物车
     * @param req
     * @param resp
     */
    protected void getCart(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        HttpSession session = req.getSession();
        Jedis redis = RedisHelper.getRedisInstance();
        JsonModel jm = new JsonModel();

        //先获取用户id
        String id = redis.get(YcConstants.SHOP_USERID + session.getId());
        if (id==null||id.equals("")||id.length()<=0){
            //没登就不查，也不报错所以1
            jm.setCode(1);
            jm.setError("未登录");
            writeJson(jm,resp);
            return;
        }
        //根据id取购物车商品编号
        String key = YcConstants.CARTITEMS+id;
        //id <=> 数量
        Map<String, String> map = redis.hgetAll(key);
        if (map==null||map.size()<=0){
            jm.setCode(1);
            jm.setError("购物车为空");
            writeJson(jm,resp);
            return;
        }

        //查商品   看看是否下架
        String sql = "select * from item where status=1 and id in (";
        for (int i = 0; i < map.size(); i++) {
            sql += "?,";
        }
        sql = sql.substring(0,sql.length()-1);
        sql += ")";
        List<Item> select = db.select(Item.class, sql, map.keySet().toArray());

        //每个商品都封装成cartItem放进购物车类
        Cart cart = new Cart();
        CartItem cartItem = null;
        List<CartItem> list = new ArrayList(); //购物车中转站
        Double total = 0.0;//总价
        Integer num = 0;//总数
        for (Item item : select) {
            cartItem = new CartItem();
            cartItem.setItem(item);

            //根据id在map里面取数量
            cartItem.setNum(Integer.valueOf(map.get(item.getId())));
            num += Integer.valueOf(map.get(item.getId()));

            //计算小计
            cartItem.getSmallCount();
            //计算总价
            total = total+cartItem.getSmallCount();

            //放入购物车
            list.add(cartItem);
        }

        //购物车准备好了
        cart.setCartItems(list);
        cart.setTotal(total);
        cart.setNum(num);
        redis.close();

        jm.setCode(1);
        jm.setObj(cart);
        writeJson(jm,resp);
    }
    /**
     * 添加购物车
     * @param req
     * @param resp
     */
    protected void addCart(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        HttpSession session = req.getSession();
        Jedis redis = RedisHelper.getRedisInstance();
        JsonModel jm = new JsonModel();

        //看看登录了没有
        String id = redis.get(YcConstants.SHOP_USERID + session.getId());
        if (id==null||id.equals("")||id.length()<=0){
            jm.setError("未登录");
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }

        //看看库里有没有 并且没有下架
        String itemId = req.getParameter("id");
        String sql = "select * from item where status=1 and id=? and stock>0";
        List<Item> select = db.select(Item.class, sql, itemId);
        if (select==null||select.size()<=0){
            jm.setCode(0);
            jm.setError("商品没有了");
            writeJson(jm,resp);
            return;
        }

        String key = YcConstants.CARTITEMS + id;
        //左边是商品id，右边是商品数量
        Map<String, String> map = redis.hgetAll(key);//购物车商品的id:数量
        if (map==null||map.size()<=0){
            //redis没有这个购物车
            map = new HashMap<>();
            map.put(itemId,"1");
        }else{
            //有东西，看看商品在不在
            Set<Map.Entry<String, String>> entries = map.entrySet();
            int flag = 0;
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getKey().equals(itemId)){
                    //如果有
                    map.put(itemId, String.valueOf(Integer.parseInt(entry.getValue())+1));//数量+1
                    flag=1;
                    break;
                }
            }
            if (flag==0){
                //遍历完了也没找到
                map.put(itemId,"1");
            }
            flag=0;
        }


        //重新
        redis.hset(key,map);

        redis.close();

        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 分页查询 可选 by price 或 rating
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected void selectByPage(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        String pageno = req.getParameter("pageno");
        String pagesize = req.getParameter("pagesize");
        String search = req.getParameter("search");//搜索关键字
        String sortby = req.getParameter("sortby");//按什么排序
        String sort = req.getParameter("sort");//排序顺序

        PageBean<Item> pageBean = new PageBean<>();
        pageBean.setPageno(Integer.parseInt(pageno));
        pageBean.setPagesize(Integer.parseInt(pagesize));
        pageBean.setSortby(sortby); //方便下次继续按这个排序
        pageBean.setSort(sort);
        pageBean.setSearch(search); //搜索关键字

        //分页查询
        PageService pageService = new PageService();
        pageBean = pageService.get(pageBean,search,sortby,sort);

        JsonModel jm = new JsonModel();

        jm.setCode(1);
        jm.setObj(pageBean);
        writeJson(jm,resp);
    }
    /**
     * 查看所有商品
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected void selectAllItems(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        List<Item> select = db.select(Item.class, "select * from item where stauts=1");
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);
    }

}
