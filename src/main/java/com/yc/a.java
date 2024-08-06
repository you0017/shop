package com.yc;

import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.dao.DbProperties;
import com.yc.utils.Sendmail;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class a {
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        DBHelper db = new DBHelper();
        String sql = "select * from userinformation";
        List<Map<String, Object>> select = db.select(sql);
        System.out.println(select);
    }
}
