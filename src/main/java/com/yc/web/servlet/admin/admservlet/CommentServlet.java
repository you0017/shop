package com.yc.web.servlet.admin.admservlet;


import com.yc.bean.Comment;
import com.yc.bean.Comments;
import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.web.model.DataModel;
import com.yc.web.model.JsonModel;
import com.yc.web.servlet.admin.bean.AdminComment;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/comment.action")
public class CommentServlet extends BaseServlet {

    // 自定义回复
    public void selfCommentReplay(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jm = new JsonModel();
        String idsStr = request.getParameter("idsStr");   // 要快速回复的评论id
        String comments = request.getParameter("comments");   // 快速回复的选项
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("UPDATE comments SET shop_backcomment_status=1, shop_backcomment='" + comments + "' WHERE id IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int result = db.doUpdate(sql.toString(), intArray);
        if (result > 0) {
            jm.setCode(1);
            jm.setObj("操作成功");
        } else {
            jm.setCode(0);
            jm.setObj("操作失败");
        }
        writeJson(jm, response);

    }


    // 我的自定义语句保存到数据字典
    public void keepSelf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jm = new JsonModel();
        String comments = request.getParameter("comments");
        String sql = "insert into datarecord (recorde_name, recorde_value, recorde_status) " +
                      " values ('shop_reply_template', ?,   1)";
        int result = db.doUpdate(sql, comments);
        if (result > 0) {
            jm.setCode(1);
            jm.setObj("保存成功");
        } else {
            jm.setCode(0);
            jm.setObj("保存失败");
        }
        writeJson(jm, response);
    }

    // 商家快速回复商品评价
    public void allCommentReplay(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonModel jm = new JsonModel();
        String idsStr = request.getParameter("idsStr");   // 要快速回复的评论id
        String selectedOption = request.getParameter("selectedOption");   // 快速回复的选项
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
//        String sql = "update comments set shop_reply=? where id in (" + idsStr + ")";
        StringBuffer sql = new StringBuffer("UPDATE comments SET shop_backcomment_status=1, shop_backcomment='" + selectedOption + "' WHERE id IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int result = db.doUpdate(sql.toString(), intArray);
        if (result > 0) {
            jm.setCode(1);
            jm.setObj("操作成功");
        } else {
            jm.setCode(0);
            jm.setObj("操作失败");
        }
        writeJson(jm, response);
    }


    // 商家回复的评论模板
    public void getBackCommentTemplate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sql = "select * from datarecord where recorde_name='shop_reply_template' and recorde_status=1";
        List<Map<String, Object>> maps = db.select(sql);
        JsonModel jm = new JsonModel();
        jm.setObj(maps);
        jm.setCode(1);
        writeJson(jm, response);
    }


    // 评论的模糊查询
    public void fuzzyQueryComment(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String shenhe = request.getParameter("shenhe");   // 审核状态  0待审核  1 不通过  2 通过
        String huifu = request.getParameter("huifu"); // 回复状态  0未回复  1已回复
        String dengji = request.getParameter("dengji");  // 星级
        String page1 = request.getParameter("page");
        String limit1 = request.getParameter("limit");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);

        StringBuffer sql = new StringBuffer("select comments.id as id, item_id, user_id, item.name AS name, " +
                "userinformation.accountname AS accountname, " +
                "comments.comment AS comment, " +
                "comments.created_at as created_at, " +
                "comments.rating as rating , " +
                "comments.shop_reply as shop_reply, " +
                "comments.shop_backcomment_status as shop_backcomment_status, " +
                "comments.shop_backcomment as shop_backcomment " +
                " FROM comments JOIN item ON comments.item_id = item.id JOIN userinformation ON comments.user_id = userinformation.id " +
                " where 1=1 ");
        List<Object> params = new ArrayList<>();
        if (shenhe != null && !"".equals(shenhe)) {
            shenhe = shenhe.trim();
            params.add(shenhe);
            sql.append(" and comments.shop_reply= ? ");
        }
        if (huifu != null && !"".equals(huifu)) {
            huifu = huifu.trim();
            params.add(huifu);
            sql.append(" and comments.shop_backcomment_status= ? ");
        }
        if (dengji != null && !"".equals(dengji)) {
            dengji = dengji.trim();
            params.add(dengji);
            sql.append(" and comments.rating= ? ");
        }
        params.add(limit);
        params.add(skip);
        sql.append(" ORDER BY comments.created_at desc  LIMIT ? OFFSET ?");
        List<AdminComment> select = db.select(AdminComment.class, sql.toString(), params.toArray());


        StringBuffer countSql = new StringBuffer("select COUNT(*) AS total " +
                "                 FROM comments JOIN item ON comments.item_id = item.id JOIN userinformation ON comments.user_id = userinformation.id  " +
                "                 where 1=1");
        List<Object> params2 = new ArrayList<>();
        if (shenhe != null && !"".equals(shenhe)) {
            shenhe = shenhe.trim();
            params2.add(shenhe);
            countSql.append(" and comments.shop_reply= ? ");
        }
        if (huifu != null && !"".equals(huifu)) {
            huifu = huifu.trim();
            params2.add(huifu);
            countSql.append(" and comments.shop_backcomment_status= ? ");
        }
        if (dengji != null && !"".equals(dengji)) {
            dengji = dengji.trim();
            params2.add(dengji);
            countSql.append(" and comments.rating= ? ");
        }
        List<Map<String, Object>> maps = db.select(countSql.toString(), params2.toArray());
        int total = Integer.parseInt(maps.get(0).get("total").toString());


        if (select != null && select.size() > 0) {
            ud.setCode(0);
            ud.setData(select);
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("暂无数据");
        }
        writeJson(ud, response);
    }

    // 商家回复单个评论
    public void shopComment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String id = request.getParameter("id");  // 商家回复的id
        String shop_backcomment = request.getParameter("editor1"); // 商家回复内容
        String selectSql = "select shop_reply from comments where id=?";
        List<Map<String, Object>> maps = db.select(selectSql, id);
        int reply_status = (int) maps.get(0).get("shop_reply");
        if (reply_status != 2) {  // 未审核
            ud.setCode(1);
            ud.setMsg("评论未审核或没通过");
            writeJson(ud, response);
            return;
        }
        String sql = "update comments set shop_backcomment=?,shop_reply=2,shop_backcomment_status=1 where id=?";
        int result = db.doUpdate(sql, shop_backcomment, id);
        if (result >= 0) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        writeJson(ud, response);
    }


    // 评论的一键操作
    public void AllCommentOperate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String operate = request.getParameter("operate");
        StringBuffer sql = null;
        if ("allOk".equals(operate)) {
            sql = new StringBuffer("update comments set shop_reply=2 where shop_reply=0");
        } else if ("allNo".equals(operate)) {
            sql = new StringBuffer("update comments set shop_reply=1 where shop_reply=0");
        }
        int result = db.doUpdate(sql.toString());
        if (result >= 0) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        writeJson(ud, response);
    }

    // 评论批量操作
    public void batchCommentEnabled(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        // 0待审核  1不通过  2通过
        String idStr = request.getParameter("idsStr");
        String operate = request.getParameter("operate");
        idStr = idStr.substring(0, idStr.length() - 1);
        String[] strArray = idStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = null;
        if ("ok".equals(operate)) {
            sql = new StringBuffer("update comments set shop_reply=2 where id in (");
            for (int i = 0; i < intArray.length - 1; i++) {
                sql.append(" ?,");
            }
            sql.append(" ?");
            sql.append(")");
        } else if ("no".equals(operate)) {
            sql = new StringBuffer("update comments set shop_reply=1 where id in (");
            for (int i = 0; i < intArray.length - 1; i++) {
                sql.append(" ?,");
            }
            sql.append(" ?");
            sql.append(")");
        }
        int result = db.doUpdate(sql.toString(), intArray);
        if (result >= 0) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        writeJson(ud, response);


    }

    // 审核不通过
    public void notReplyComment(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String user_id = request.getParameter("user_id");  // 用户id
        String item_id = request.getParameter("item_id");  // 商品id
        String id = request.getParameter("id");  // 评论id
        String sql = "update comments set shop_reply=1 where id=?";  // 更新回复状态
        int result = db.doUpdate(sql, id);   // 不通过的
        if (result > 0) {
            ud.setCode(0);
            ud.setMsg("审核成功");
        } else {
            ud.setCode(1);
            ud.setMsg("审核失败");
        }
        writeJson(ud, response);
    }

    // 审核评论通过
    public void replyComment(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String user_id = request.getParameter("user_id");  // 用户id
        String item_id = request.getParameter("item_id");  // 商品id
        String id = request.getParameter("id");  // 评论id

        String sql2 = "update comments set shop_reply=2 where id=?";  // 更新回复状态
        int result2 = db.doUpdate(sql2, id);   // 通过的
        if (result2 > 0) {
            ud.setCode(0);
            ud.setMsg("审核成功");
        } else {
            ud.setCode(1);
            ud.setMsg("审核失败");
        }
        writeJson(ud, response);
    }


    public void getAllComment(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        ud = allCommentData(ud, request);
        writeJson(ud, response);
        System.out.println("我来要评论来了");
    }

    // 获取所有用户评论数据的方法
    private static DataModel allCommentData(DataModel ud, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 获取总条数
        String totalSql = "select count(*) total from comments ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        // user_id!= 0 代表是用户评论
//        String limitSql = "select * from comments limit ? offset ?";
        String limitSql = "select comments.id as id, item_id, user_id, item.name AS name, " +
                "userinformation.accountname AS accountname, " +
                "comments.comment AS comment, " +
                "comments.created_at as created_at, " +
                "comments.rating as rating , " +
                "comments.shop_reply as shop_reply, " +
                "comments.shop_backcomment_status as shop_backcomment_status, " +
                "comments.shop_backcomment as shop_backcomment " +
                " FROM comments JOIN item ON comments.item_id = item.id JOIN userinformation ON comments.user_id = userinformation.id " +
                " where comments.parent_comment_id=0 ORDER BY comments.created_at desc limit ? offset ?";
        List<AdminComment> limitMaps = db.select(AdminComment.class, limitSql, limit, skip);
//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");
        if (limitMaps != null && limitMaps.size() > 0) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }


}
