package com.yc.web.servlet.admin.admservlet;



import com.sun.crypto.provider.DHKeyAgreement;
import com.yc.web.model.DataModel;
import com.yc.web.servlet.admin.bean.Logistics;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;


@WebServlet("/admin/logistics.action")
public class LogisticsServlet extends BaseServlet{


    public void getAllLogisticsData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        String allLogisticsSql = "select * from logistics ORDER BY shipping_date DESC limit ? offset ?";
        String allLogisticsSqlTotal = "select count(*) as total from logistics";
        List<Logistics> select = db.select(Logistics.class, allLogisticsSql, limit, skip);
        List<Map<String, Object>> maps = db.select(allLogisticsSqlTotal);
        int total = Integer.parseInt( maps.get(0).get("total").toString());
        if (select!=null && select.size()>0) {
            ud.setData(select);
            ud.setCode(0);
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("没有数据");
        }
        writeJson(ud, response);
    }
}
