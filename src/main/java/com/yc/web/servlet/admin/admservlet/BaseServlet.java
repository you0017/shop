package com.yc.web.servlet.admin.admservlet;


import com.google.gson.Gson;
import com.yc.dao.DBHelper;
import com.yc.web.model.JsonModel;
import com.yc.web.model.DataModel;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public abstract class BaseServlet extends HttpServlet {
    protected DBHelper db = new DBHelper();

//    利用反射获取参数
    protected <T> T parseObjectFromRequest(HttpServletRequest request, Class<T> cls) throws InstantiationException, IllegalAccessException, InvocationTargetException {
//        1、 创建 T
        T t = cls.newInstance();
//        2、取出request中所有的参数
        Map<String, String[]> parameterMap = request.getParameterMap();  // 取出所有的参数
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            String methodname = "set" + key.substring(0,1).toUpperCase() + key.substring(1); // setXxxresfood
            Method setMethod = findSetMethod(methodname, cls);  // 找setXxx
            if ( setMethod==null ) {
                continue;
            }
            // 激活setMethod
//            int intValue = 0;
//            if ( isNumeric(value ) ) {
//                intValue = Integer.parseInt(value);
//                setMethod.invoke(t, intValue);
//            }else {
                setMethod.invoke(t, value);
//            }

        }
        return t;
    }

    private <T> Method findSetMethod(String methodName, Class<T> c) {
        // 找出所有方法
        Method[] ms = c.getDeclaredMethods();

        for (Method m : ms) {
//            如果我要找的方法名在这个反射对象返回的方法中找到了
            if (methodName.equals(m.getName())) {
                return m;
            }
        }
        return null;

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String op = req.getParameter("op");
        JsonModel jm = new JsonModel();
        try {
            if (op == null || "".equals(op)) {
                jm.setCode(0);
                jm.setError("op参数不能为空");
                writeJson(jm, resp);
                return;
            }
//           反射
            Method[] ms = this.getClass().getDeclaredMethods();   // 这里要用getDeclaredMethods
            for (Method m : ms) {
                if ( m.getName().equals(op) ){
                    m.invoke(this, req, resp);
                }
            }
        }catch ( Exception ex){
            ex.printStackTrace();
            jm.setCode(0);
            jm.setError( ex.getMessage() );
            writeJson(jm, resp);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    protected void writeJson( JsonModel jm, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/json:charset=utf-8");
        PrintWriter out = resp.getWriter();
        Gson g = new Gson();  // 一个java库，可以将java对象转换为json格式的字符串，也可以转回来
        out.println(g.toJson(jm));   // jm对象转为Json字符串传到前端
        out.flush();
        out.close();
    }

    protected void writeJson(DataModel ud, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/json:charset=utf-8");
        PrintWriter out = resp.getWriter();
        Gson g = new Gson();  // 一个java库，可以将java对象转换为json格式的字符串，也可以转回来
        out.println(g.toJson(ud));   // jm对象转为Json字符串传到前端
        out.flush();
        out.close();
    }


    // 菜单数据
    protected void writeObjectJson(Object obj, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/json:charset=utf-8");
        PrintWriter out = resp.getWriter();
        Gson g = new Gson();  // 一个java库，可以将java对象转换为json格式的字符串，也可以转回来
        out.println(g.toJson(obj));   // jm对象转为Json字符串传到前端
        out.flush();
        out.close();
    }


    private static boolean isNumeric(String str) {
        if ( str.matches("-?\\d+(\\.\\d+)?") && str.length()<=5){
            return true;
        }else {
            return false;
        }
    }
}
