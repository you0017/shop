package com.yc.web.servlet.admin.admservlet;


import com.yc.bean.DataRecord;
import com.yc.dao.DBHelper;
import com.yc.web.model.DataModel;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

// 设置平台的属性
@WebServlet("/admin/frontEdit.action")
public class FrontEdit extends BaseServlet {

    // 数据字典的选择下拉框
    public void getKindName(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String value = request.getParameter("value");
        String data = request.getParameter("data");
        String name = request.getParameter("name");

        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 查总有多少条数据J  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from datarecord ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select * from datarecord where recorde_name= ? limit ? offset ?";
        List<DataRecord> limitMaps = db.select(DataRecord.class, limitSql, name, limit, skip);
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
        writeJson(ud, response);
    }

    // getAllKind
    public void getAllKind(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        String sql = "select DISTINCT  recorde_name from datarecord";
        List<Map<String, Object>> list = db.select(sql);
        DataModel ud = new DataModel();
        ud.setData(list);
        writeJson(ud, response);

    }

    // 修改参数设置
    public void editSystemParameter(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, IOException {
        DataModel ud = new DataModel();
        String recordeId = request.getParameter("id");
        String recordeName = request.getParameter("recorde_name");
        String recordeValue = request.getParameter("recorde_value");
        String status = request.getParameter("recorde_status");
        String sql = "update datarecord set recorde_name=?, recorde_value=?, recorde_status=? where id=?";
        int result = db.doUpdate(sql, recordeName, recordeValue, status, recordeId);
        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        writeJson(ud, response);
    }

    // 删除参数设置
    public void deleteSystemParameter(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, IOException {
        DataModel ud = new DataModel();
        String id = request.getParameter("idsStr");
        String sql = "delete from datarecord where id  = ?";
        int result = db.doUpdate(sql, id);
        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        writeJson(ud, response);
    }

    // 添加参数设置
    public void addSystemParameter(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, IOException {
        DataModel ud = new DataModel();
        String recordeName = request.getParameter("recorde_name");
        String recordeValue = request.getParameter("recorde_value");
        String status = request.getParameter("recorde_status");
        String sql = "insert into datarecord(recorde_name, recorde_value, recorde_status) values (?, ?, ?)";
        int result = db.doUpdate(sql, recordeName, recordeValue, status);
        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("成功");
        } else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        writeJson(ud, response);
    }


    // 拿到全部数据
    public void getAllSystemInfo(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        ud = getInfo(ud, request);
        writeJson(ud, response);
    }


    // 查询初始数据
    public static DataModel getInfo(DataModel ud, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 查总有多少条数据J  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from datarecord ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select * from datarecord limit ? offset ?";
        List<DataRecord> limitMaps = db.select(DataRecord.class, limitSql, limit, skip);
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
