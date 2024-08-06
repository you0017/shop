package com.yc.web.servlet.admin.strategy;

import com.yc.dao.DBHelper;
import com.yc.web.model.DataModel;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;

public interface DataStrategy {
    DBHelper db = new DBHelper();
    // 根据sql查询初始数据
    public DataModel initData(DataModel ud, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, InstantiationException;
}
