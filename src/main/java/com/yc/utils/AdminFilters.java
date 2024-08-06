package com.yc.utils;

import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

// 访问后台的过滤器
//@WebFilter("/admin/*")
public class AdminFilters implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        String requestURI = req.getRequestURI();

        if ( session.getAttribute("admin")!=null || session.getAttribute("sell")!=null || requestURI.equals("/shop_war/admin/pages/login.html")) {
            chain.doFilter(req, resp);
        }else {
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("text/json:charset=utf-8");
            HashMap<String, Object> map = new HashMap<>();
            map.put("code", 1);
            map.put("msg", "访问后台权限不足");
            PrintWriter out = resp.getWriter();
            Gson g = new Gson();  // 一个java库，可以将java对象转换为json格式的字符串，也可以转回来
            out.println(g.toJson(map));   // jm对象转为Json字符串传到前端
            out.flush();
            out.close();
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
