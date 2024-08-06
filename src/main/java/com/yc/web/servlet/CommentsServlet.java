package com.yc.web.servlet;

import com.yc.bean.Comment;
import com.yc.bean.CommentList;
import com.yc.bean.Comment_max;
import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.dao.RedisHelper;
import com.yc.utils.AliOSSProperties;
import com.yc.utils.AliOSSUtils;
import com.yc.utils.YcConstants;
import com.yc.web.model.JsonModel;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@WebServlet("/html/comment.action")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class CommentsServlet extends BaseServlet{

    private DBHelper db = new DBHelper();

    /**
     * 点赞或踩
     * @param req
     * @param resp
     */
    protected void likes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");//评论id
        String index = req.getParameter("index");
        JsonModel jm = new JsonModel();
        int i;
        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        if (index.equals("1")){
            //点赞
            if (redis.sismember(id+YcConstants.LIKES_COMMENT,user_id+"")){
                //此用户已经点赞过，再点就是取消
                redis.srem(id+YcConstants.LIKES_COMMENT,user_id+"");   //用户 -> [多个评论id]
                //用户编号 此处也要删除
                redis.srem(user_id+YcConstants.LIKES_USER,id+"");//评论 -> [多个用户id]

                //更新数据库，方便看，主要是懒得改查询代码了
                i = db.doUpdate("update comments set likes=likes-1 where id=?", id);
            }else{
                //没有点赞过
                redis.sadd(id+YcConstants.LIKES_COMMENT,user_id+"");//用户 -> [多个评论id]
                redis.sadd(user_id+YcConstants.LIKES_USER,id+"");//评论 -> [多个用户id]

                i = db.doUpdate("update comments set likes=likes+1 where id=?", id);
            }
        }else{
            //踩
            if (redis.sismember(id+YcConstants.DISLIKES_COMMENT,user_id+"")){
                //此用户已经点踩过，再点就是取消
                redis.srem(id+YcConstants.DISLIKES_COMMENT,user_id+"");
                //用户编号 此处也要删除
                redis.srem(user_id+YcConstants.DISLIKES_USER,id+"");

                i = db.doUpdate("update comments set dislikes=dislikes-1 where id=?",id);
            }else{
                //没有点踩过
                redis.sadd(id+YcConstants.DISLIKES_COMMENT,user_id+"");
                redis.sadd(user_id+YcConstants.DISLIKES_USER,id+"");

                i = db.doUpdate("update comments set dislikes=dislikes+1 where id=?",id);
            }


        }
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 获取评论
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void getComments(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        String id = req.getParameter("id");//商品id
        String pageno = req.getParameter("pageno");
        String pagesize = req.getParameter("pagesize");
        String sortby = req.getParameter("sortby");
        String sort = req.getParameter("sort");

        JsonModel jm = new JsonModel();

        Comment_max commentMax = new Comment_max(); //最外层
        commentMax.setPageno(Integer.parseInt(pageno));
        commentMax.setPagesize(Integer.parseInt(pagesize));
        commentMax.setSortby(sortby);
        commentMax.setSort(sort);

        List<Comment> select = db.select(Comment.class, "select * from comments where item_id=? and parent_comment_id=0 and shop_reply=2",id);
        //查所有，为评分服务，分页评分有问题，不全，只能单独算
        for (Comment comments : select) {
            //顺便看一下打分个数
            if (comments.getRating()==5){
                commentMax.setFive(commentMax.getFive()+1);
            } else if (comments.getRating()==4) {
                commentMax.setFour(commentMax.getFour()+1);
            } else if (comments.getRating()==3) {
                commentMax.setThree(commentMax.getThree()+1);
            } else if (comments.getRating()==2) {
                commentMax.setTwo(commentMax.getTwo()+1);
            } else if (comments.getRating()==1) {
                commentMax.setOne(commentMax.getOne()+1);
            }
        }
        commentMax.setComment_count((long) select.size());
        int main_comments = select.size();
        for (Comment comments : select) {
            //副评论
            List<Comment> childs = db.select(Comment.class, "select * from comments where parent_comment_id=? and shop_reply=2 order by id desc", comments.getId());
            //List<Map<String, Object>> select4 = db.select("select count(*) from comments where parent_comment_id=? and shop_reply=1", comments.getId());

            commentMax.setComment_count(commentMax.getComment_count()+childs.size());//每次遍历都要把副评论数量算进去

            for (Comment child : childs) {
                //顺便看一下打分个数
                if (child.getRating()==5){
                    commentMax.setFive(commentMax.getFive()+1);
                } else if (child.getRating()==4) {
                    commentMax.setFour(commentMax.getFour()+1);
                } else if (child.getRating()==3) {
                    commentMax.setThree(commentMax.getThree()+1);
                } else if (child.getRating()==2) {
                    commentMax.setTwo(commentMax.getTwo()+1);
                } else if (child.getRating()==1) {
                    commentMax.setOne(commentMax.getOne()+1);
                }
            }
        }

        /**
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         * 以上是计算评论数量及评分各级数量
         */

        String sql1 = "select *        from comments where item_id=? and parent_comment_id = 0 and shop_reply = 2 order by "+sortby+" "+sort+" limit ? offset ?";
        //String sql2 = "select count(*) from comments where item_id=? and parent_comment_id = 0 and shop_reply = 1";

        int i = (commentMax.getPageno() - 1) * commentMax.getPagesize();

        //查询，并且要父评论id为0                                           返回几个    从第几个开始
        select = db.select(Comment.class, sql1,id,commentMax.getPagesize(),i);//查这个商品所有的评论
        //List<Map<String, Object>> select2 = db.select(sql2, id);//查这个商品评论数量

        if (select==null||select.size()<=0){
            jm.setCode(1);
            jm.setObj(commentMax);
            writeJson(jm,resp);
            return;
        }
        //(Long.valueOf(select2.get(0).get("count(*)").toString()));//数量  还差回复评论

        //计算页数  只根据主页数算
        commentMax.setTotalpages((int) (main_comments % commentMax.getPagesize() == 0 ? main_comments / commentMax.getPagesize() : main_comments / commentMax.getPagesize() + 1));

        //计算上一页
        if (commentMax.getPageno()==1 || commentMax.getTotalpages()==0){
            commentMax.setPre(1);
        }else{
            commentMax.setPre(commentMax.getPageno()-1);
        }

        //计算下一页
        if (commentMax.getPageno()==commentMax.getTotalpages() || commentMax.getTotalpages()==0){
            //最后一页
            commentMax.setNext(commentMax.getPageno());
        }else{
            commentMax.setNext(commentMax.getPageno()+1);
        }

        List<CommentList> commentLists = new ArrayList<>();//最外层的属性，需要把所有主评论加进去，然后放入最外层
        CommentList commentList = null;//主评论+副评论集合

        //找名字
        for (Comment comments : select) {

            comments.setCreated_at(comments.getCreated_at().substring(0,comments.getCreated_at().length()-2));//时间格式化一下


            List<Map<String, Object>> select1 = db.select("select name,image from userinformation where id = ?", comments.getUser_id());
            comments.setUser_name((String) select1.get(0).get("name"));
            comments.setImage((String) select1.get(0).get("image"));//单个主评论已经完整

            //主评论添加到  主评论+副评论集合
            commentList = new CommentList();
            commentList.setComment(comments);//主评论

            //副评论
            List<Comment> childs = db.select(Comment.class, "select * from comments where parent_comment_id=? and shop_reply=2 order by id desc", comments.getId());

            for (Comment child : childs) {
                //时间格式化一下
                child.setCreated_at(child.getCreated_at().substring(0,child.getCreated_at().length()-2));


                List<Map<String, Object>> select3 = db.select("select name,image from userinformation where id = ?", child.getUser_id());
                child.setUser_name((String) select3.get(0).get("name"));
                child.setImage((String) select3.get(0).get("image"));//单个副评论完整
            }
            commentList.setComment_2(childs);//该主评论的副评论已经完整，可以追加到  主评论+副评论集合
            commentLists.add(commentList);
        }
        //所有主评论及其对应副评论遍历完
        commentMax.setCommentLists(commentLists);

        jm.setCode(1);
        jm.setObj(commentMax);
        writeJson(jm,resp);
    }

    /**
     * 评论
     * @param req
     * @param resp
     */
    protected void remark(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        String id1 = req.getParameter("id");//主评论id
        String topic = req.getParameter("topic");
        String id = req.getParameter("item_id");
        String rating = req.getParameter("rating");
        String remark = req.getParameter("remark");

        Jedis redis = RedisHelper.getRedisInstance();
        String user_id = redis.get(YcConstants.SHOP_USERID + req.getSession().getId());
        redis.close();

        JsonModel jm = new JsonModel();

        /**
         * 要看这个用户有没有购买这个商品
         */
        List<Map<String, Object>> select = db.select("select * from `order` left join order_detail on `order`.id=order_detail.order_id where order_detail.item_id=? and `order`.user_id=?", id, user_id);
        if (select==null||select.size()<=0){
            jm.setCode(0);
            jm.setError("未购买此商品，无法评论");
            writeJson(jm,resp);
            return;
        }


        //都存商品id，方便后面算分，但是取得时候只取主评论
        int i = db.doUpdate("insert into comments(item_id,user_id,topic,comment,rating,parent_comment_id) values(?,?,?,?,?,?)", id, user_id, topic, remark, rating,id1);
        if (i<=0){
            jm.setCode(0);
            writeJson(jm,resp);
            return;
        }
        jm.setCode(1);
        writeJson(jm,resp);
    }

    /**
     * 图片上传
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    protected void pic(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
        List<Part> parts = (ArrayList) req.getParts();
        AliOSSUtils aliOSSUtils = new AliOSSUtils(new AliOSSProperties());
        JsonModel jm = new JsonModel();
        List urls = new ArrayList();

        try {
            String url = aliOSSUtils.upload(parts.get(0));
            urls.add(url);
        } catch (IOException e) {
            jm.setCode(0);
            jm.setError("上传失败");
            try {
                writeJson(jm,resp);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        Map map = new HashMap();
        map.put("uploaded","1");
        map.put("url",urls.get(0));
        writeObjectToJson(map,resp);
    }
}
