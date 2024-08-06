package com.yc.web.servlet;

import com.yc.bean.DataRecord;
import com.yc.dao.DBHelper;
import com.yc.web.model.JsonModel;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/html/normal.action")
public class NormalServlet extends BaseServlet{

    private DBHelper db = new DBHelper();

    /**
     * 用于websocket动态获取网址
     * @param request
     * @return
     */
    protected void getServerInfo(HttpServletRequest request,HttpServletResponse response) throws IOException {
        Map<String, String> serverInfo = new HashMap<>();
        String protocol = request.isSecure() ? "wss" : "ws";
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();

        serverInfo.put("protocol", protocol);
        serverInfo.put("host", host);
        serverInfo.put("port", String.valueOf(port));
        serverInfo.put("contextPath", contextPath);

        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(serverInfo);
        writeJson(jm,response);
    }

    /**
     * 数据字典之类的
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected void normal(HttpServletRequest req , HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        List<DataRecord> select = db.select(DataRecord.class, "select * from datarecord where recorde_status = 1");
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        jm.setObj(select);
        writeJson(jm,resp);
    }
}
