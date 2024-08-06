package com.yc.web.servlet.admin.strategy;


import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.web.model.DataModel;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

//
public class UserStrategy implements DataStrategy{

    // 用户的所有数据
    @Override
    public DataModel initData(DataModel ud, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据  select count(*) total from UserInformation;
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
