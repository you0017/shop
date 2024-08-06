package com.yc.web.servlet.admin.admservlet;


import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.utils.EncryptUtils;
import com.yc.web.model.DataModel;
import com.yc.web.servlet.admin.strategy.UserStrategy;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/user.action")
public class UserServlet extends BaseServlet {

    // 编辑更新
    public void edit (HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel dm = new DataModel();
        String id = request.getParameter("id");
        String accountname = request.getParameter("accountname");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String status = request.getParameter("status");
        password = EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password));
        String sql = "update userinformation set ";
        List<Object> params = new ArrayList<>();
        if (accountname != null && accountname.trim().isEmpty() == false) {
            accountname = accountname.trim();
            params.add(accountname);
            sql += " accountname = ?, ";
        }
        if (name != null && name.trim().isEmpty() == false) {
            name = name.trim();
            params.add(name);
            sql += " name = ?, ";
        }
        if (password != null && password.trim().isEmpty() == false) {
            password = password.trim();
            params.add(password);
            sql += " password = ?, ";
        }
        if (email != null && email.trim().isEmpty() == false) {
            email = email.trim();
            params.add(email);
            sql += " email = ?, ";
        }
        if (role != null && role.trim().isEmpty() == false) {
            role = role.trim();
            params.add(role);
            sql += " role = ?, ";
        }
        if (status != null && status.trim().isEmpty() == false) {
            status = status.trim();
            params.add(status);
            sql += " status = ? ";
        }
        id  = id.trim();
        params.add(id);
        sql += "where id = ?";
        int result = db.doUpdate(sql,params.toArray());
        if ( result>=1 ) {
            dm.setCode(0);
            dm.setMsg("用户编辑更新成功");
        }else {
            dm.setCode(1);
        }
        writeJson(dm, response);
    }


    // 添加
    public void addUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel dm = new DataModel();
        String accountname = request.getParameter("accountname");
        String password = request.getParameter("password");
        password = EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password));
        String name = request.getParameter("name");
        String status = request.getParameter("status");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        // 注意库里不能有空值
        String sql = "INSERT INTO userinformation (accountname, password, name, status, email,  role, logincount) VALUES ( ?,?,?,?,?,?,0 )";
        int i = db.doUpdate(sql, accountname, password, name, status, email, role);
        if (i>=1){
            dm.setCode(0);
        }else {
            dm.setCode(1);
        }
        writeJson(dm,response);
    }

    // 批量删除
    public void batchDel(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
//        DELETE FROM UserInformation WHERE ID IN (1, 2, 3);
        StringBuffer sql = new StringBuffer("DELETE FROM userinformation WHERE ID IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i >= 1) {
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("删除失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 删除一个
    public void del(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        StringBuffer sql = new StringBuffer("DELETE FROM userinformation WHERE ID = ? ");
        int i = db.doUpdate(sql.toString(), idsStr);
        if (i >= 1) {
            ud.setCode(0);
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);

    }

    //  批量停用
    public void batchDisabled(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("UPDATE userinformation SET Status = 0 WHERE ID IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i >= 1) {
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 设置启用多个
    public void batchEnabled(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("UPDATE userinformation SET status = 1 WHERE id IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i >= 1) {
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 模糊查询
    public void fuzzyQuery(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String accountname = request.getParameter("accountname");// 账号
        String name = request.getParameter("name");  // 名字
        String status = request.getParameter("status");// 状态

        String sql = "select * from userinformation where 1=1";
        List<Object> params = new ArrayList<>();
        if (accountname != null && accountname.trim().isEmpty() == false) {
            accountname = accountname.trim();
            params.add(accountname);
            sql += " and accountname like concat('%',?,'%') ";
        }
        if (name != null && name.trim().isEmpty() == false) {
            name = name.trim();
            params.add(name);
            sql += " and name like concat('%',?,'%') ";
        }
        if (status != null && status.trim().isEmpty() == false) {
            status = status.trim();
            params.add(status);
            sql += " and status = ? ";
        }

        if ( startTime!=null && !"".equals(startTime) &&  endTime!=null && !"".equals(endTime)  ){  // 都有
            startTime = startTime.trim();
            endTime = endTime.trim();
            params.add(startTime);
            params.add(endTime);
            sql += " and CreationTime BETWEEN  ? and ?";
        }else if (startTime!=null && !"".equals(startTime)){ // 有开始
            startTime = startTime.trim();
            params.add(startTime);
            sql += " and CreationTime > ?";
        }else if ( endTime!=null && !"".equals(endTime) ){ // 有结束
            endTime = endTime.trim();
            params.add(endTime);
            sql += " and CreationTime < ?";
        }else if ( startTime==null && "".equals(startTime) && endTime==null && "".equals(endTime)){
            sql += "";
        }
        List<UserInformation> select = db.select(UserInformation.class, sql, params.toArray());
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

    // 用户管理初始数据获取
    public void getAllUserData(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        DataModel ud = new DataModel();
//        ud = UserData.allUserData(ud, request);
        UserStrategy us = new UserStrategy();
       ud = us.initData(ud, request);
//        ud = allUserData(ud, request);
        writeJson(ud, response);
    }

    // 获取所有用户数据的方法
    private static DataModel allUserData (DataModel ud, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据J  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from userinformation ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString()) ;
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select * from userinformation limit "+limit+" offset " + skip;
        List<UserInformation> limitMaps = db.select(UserInformation.class, limitSql);
//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");
        if ( limitMaps!=null && limitMaps.size()>0 ) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }

}
