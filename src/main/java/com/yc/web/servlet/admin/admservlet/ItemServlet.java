package com.yc.web.servlet.admin.admservlet;


import com.yc.bean.Item;
import com.yc.bean.UserInformation;
import com.yc.dao.DBHelper;
import com.yc.utils.OssUtils;
import com.yc.web.model.DataModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet("/admin/item.action")
public class ItemServlet extends BaseServlet {
    String fossUrl = "";  // 主图
    String fossUrl1 =""; // 附图

    // 联动产品名
    public void getMyName(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String slq = "select name from item";
        List<Map<String, Object>> maps = db.select(slq);
        ud.setData(maps);
        ud.setCode(0);
        writeJson(ud, response);
    }


    // 规格联动拿到配置
    public void getConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String cityValue = request.getParameter("cityValue");   // 配置
        String sql = "select * from datarecord where recorde_name = ? and recorde_status=1";
        List<Map<String, Object>> maps = db.select(sql, cityValue);
        ud.setData(maps);
        ud.setCode(0);
        writeJson(ud, response);
    }


    // 规格联动拿到品牌
    public void getBrand(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String Brand = request.getParameter("provinceValue");
        String sql = "select * from datarecord where recorde_name = ? and recorde_status=1";
        List<Map<String, Object>> maps = db.select(sql, Brand);
        ud.setData(maps);
        ud.setCode(0);
        System.out.println("getBrand");
        writeJson(ud, response);
    }

    // 规格联动拿到种类
    public void getAllKind(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String sql = "select * from datarecord where recorde_name = '种类' and recorde_status=1";
        List<Map<String, Object>> maps = db.select(sql);
        ud.setData(maps);
        ud.setCode(0);
//        System.out.println("getAllKind");
        writeJson(ud, response);
    }






    // 提取路径
    public static String extractImagePath(String html) {
        String pattern = "<img[^>]*src=\"([^\"]*)\"[^>]*>";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    // 上传图片
    public void pic(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Part> parts = (ArrayList)request.getParts();
        System.out.println("pic");
        OssUtils ossUtils = new OssUtils();
        String picUrl=null;
        try {
             picUrl = ossUtils.upload(parts.get(0));
        }catch (Exception e) {
            e.printStackTrace();
        }

        Map map = new HashMap();
        map.put("uploaded","1");
        map.put("url",picUrl);
        writeObjectJson(map,response);

    }

    // 全部上架
    public void allRise(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String sql = "update item set status=1 where 1=1";
        int count = db.doUpdate(sql);
        if (count > 0 ) {
            ud.setCode(0);
            ud.setData("成功");
            ud.setMsg("全部上架成功");
        }else {
            ud.setCode(1);
            ud.setData("失败");
            ud.setMsg("全部上架失败");
        }
        writeJson(ud,response);
    }

    // 全部下架
    public void allDel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel ud = new DataModel();
        String sql = "update item set status=0 where 1=1";
        int count = db.doUpdate(sql);
        if (count > 0 ) {
            ud.setCode(0);
            ud.setData("成功");
            ud.setMsg("全部下架成功");
        }else {
            ud.setCode(1);
            ud.setData("失败");
            ud.setMsg("全部下架失败");
        }
        writeJson(ud,response);
    }

    // 查询功能
    public void fuzzyQuery(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String id = request.getParameter("id");
        String name = request.getParameter("name");  // 名字
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        List<Object> params = new ArrayList<>();

        String sql = "select * from item where 1=1 ";
        if (id != null && id.trim().isEmpty() == false) {
            id = id.trim();
            params.add(id);
            sql += " and id = ? ";
        }

        if (name != null && name.trim().isEmpty() == false) {
            name = name.trim();
            params.add(name);
            sql += " and name like '%' ? '%' ";
        }


        if ( startTime!=null && !"".equals(startTime) &&  endTime!=null && !"".equals(endTime)  ){  // 都有
            startTime = startTime.trim();
            endTime = endTime.trim();
            params.add(startTime);
            params.add(endTime);
            sql += " and create_time BETWEEN  ? and ?";
        }else if (startTime!=null && !"".equals(startTime)){ // 有开始
            startTime = startTime.trim();
            params.add(startTime);
            sql += " and create_time > ?";
        }else if ( endTime!=null && !"".equals(endTime) ){ // 有结束
            endTime = endTime.trim();
            params.add(endTime);
            sql += " and create_time < ?";
        }else if ( startTime==null && "".equals(startTime) && endTime==null && "".equals(endTime)){
            sql += "";
        }
        List<Item> select = db.select(Item.class, sql, params.toArray());
        int total = select.size();
        if ( select!=null && select.size()>0 ) {
            ud.setData(select);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("无您想查的数据");
        }
        writeJson(ud, response);
    }

    // 上传副图
    public void uploadSeveral(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        DataModel pd = new DataModel();  // 写出图片传输的结果
//        OssUtils ossUtils = new OssUtils();
//        Part filePart = request.getPart("file");  // 拿到前端的图片数据
//        String name = filePart.getSubmittedFileName();   // 提交的名字
//        name = "item/picture/" + name;
//        InputStream inputStream = filePart.getInputStream();  // 输出流
//        String itemOssUrl = "https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/";
//        Boolean result = ossUtils.UploadImageToOSS( name, inputStream); // 上传到oss的结果
//        if ( result){
//            this.fossUrl = itemOssUrl+name;
//            pd.setCode(0);
//        }
//        writeJson(pd, response);
    }

    // 上传单张item图片
    public void uploadInst(HttpServletRequest request, HttpServletResponse response) throws Exception {
        DataModel pd = new DataModel();  // 写出图片传输的结果
        OssUtils ossUtils = new OssUtils();
        Part filePart = request.getPart("file");  // 拿到前端的图片数据
        String name = filePart.getSubmittedFileName();   // 提交的名字
        name = "item/picture/" + name;
        InputStream inputStream = filePart.getInputStream();  // 输出流
        String itemOssUrl = "https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/";
        Boolean result = ossUtils.UploadImageToOSS( name, inputStream); // 上传到oss的结果
        String temp =itemOssUrl+name;
        if ( result ){
            pd.setData(temp);
            pd.setCode(0);
        }
        writeJson(pd, response);
    }

    // 添加产品
    public void addProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel dm = new DataModel();
        String name = request.getParameter("name");  // 名称
        String price = request.getParameter("price");   // 价格
        String stock = request.getParameter("stock");  // 库存
        String category = request.getParameter("category");  // 类别
        String brand = request.getParameter("brand");  // 品牌
        String spec = request.getParameter("spec");  // 描述
        String status = request.getParameter("status");  // 状态
        String pic = request.getParameter("pic");  // 图片地址
        String item_details = request.getParameter("editor1");   // 商品描述
        // 注意库里不能有空值
        String sql="INSERT INTO item (name, price, stock, category, brand,  spec, image, status, item_details) " +
                  "VALUES ( ? ,? ,?, ?, ?, ?, ?, ?, ?)";
        int i = db.doUpdate(sql, name, price, stock, category, brand, spec, pic, status, item_details);  // 添加数据的结果
        if (i>=1){
            dm.setCode(0);
            dm.setMsg("添加成功");
        }else {
            dm.setCode(1);
            dm.setMsg("添加失败");
        }
        writeJson(dm,response);
    }


    // 产品修改功能  附图不知道咋弄
    public void edit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel dm = new DataModel();
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String price = request.getParameter("price");
        String status = request.getParameter("status");
        String stock = request.getParameter("stock");
        String pic = request.getParameter("pic");  // 图片地址
        String sql = "update item set ";
        List<Object> params = new ArrayList<>();
        if (name != null && name.trim().isEmpty() == false) {
            name = name.trim();
            params.add(name);
            sql += " name = ?, ";
        }
        if (price != null && price.trim().isEmpty() == false) {
            price = price.trim();
            params.add(price);
            sql += " price = ?, ";
        }
        if (status != null && status.trim().isEmpty() == false) {
            status = status.trim();
            params.add(status);
            sql += " status = ? ,";
        }
        if (stock!= null && stock.trim().isEmpty() == false) {
            stock = stock.trim();
            params.add(stock);
            sql += " stock =?,";
        }

        if (pic != null && pic.trim().isEmpty() == false) {
            pic = pic.trim();
            params.add(pic);
            sql += " image = ? ,";
        }
        sql = sql.substring(0, sql.length() - 1);
        id = id.trim();
        params.add(id);
        sql += "  where id = ?";
        int result = db.doUpdate(sql, params.toArray());
        if (result >= 1) {
            dm.setCode(0);
            dm.setMsg("item编辑更新成功");
        } else {
            dm.setCode(1);
        }

//        if ( this.fossUrl!="" ){  // 主图
//            String imageSql = "update item set image = ? where id = ?";
//            db.doUpdate(imageSql, this.fossUrl, id);
//            this.fossUrl="";
//        }

//        if ( this.fossUrl1!=""){
//            String imageSql = "update itempic set image = ? where itemid = ?";
//            db.doUpdate(imageSql, this.fossUrl1, id);
//            this.fossUrl1="";
//        }
        writeJson(dm, response);
    }


    public void consoleInit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel dm = new DataModel();
        String pamar = request.getParameter("param");
        if ("income".equals(pamar)) {
            dm.setCode(0);
            dm.setMsg("20000");
        } else if ("goods".equals(pamar)) {
            String sql = "select count(*) goods from item";
            List<Map<String, Object>> maps = db.select(sql);
            int goodsTotal = Integer.parseInt(maps.get(0).get("goods").toString());
            dm.setCode(0);
            dm.setMsg(String.valueOf(goodsTotal));
        } else if ("user".equals(pamar)) {
            String sql = "select  count(*) usertotal from userinformation";
            List<Map<String, Object>> maps = db.select(sql);
            int userTotal = Integer.parseInt(maps.get(0).get("usertotal").toString());
            dm.setMsg(String.valueOf(userTotal));
            dm.setCode(0);
        } else {
            dm.setMsg("xxx");
            dm.setCode(0);
        }
        writeJson(dm, response);
    }


    // 批量删除
    public void batchDelete(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("DELETE FROM item WHERE ID IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i >= 1) {
            ud = allItemData(ud, request);
            ud.setCode(0);
        } else {
            ud.setMsg("删除失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 删除一个
    public void deleteById(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        StringBuffer sql = new StringBuffer("DELETE FROM item WHERE ID = ? ");
        int i = db.doUpdate(sql.toString(), idsStr);
        if (i >= 1) {
            ud = allItemData(ud, request);
            ud.setCode(0);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 批量停用
    public void batchDisabled(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
//        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("UPDATE item SET status = 0 WHERE ID IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i >= 1) {
            ud = allItemData(ud, request);
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
//        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        StringBuffer sql = new StringBuffer("UPDATE item SET status = 1 WHERE id IN ( ");
        for (int i = 0; i < intArray.length - 1; i++) {
            sql.append(" ?,");
        }
        sql.append(" ?");
        sql.append(")");
        int i = db.doUpdate(sql.toString(), intArray);
        if (i >= 1) {
            ud = allItemData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        writeJson(ud, response);
    }

    // 商品初始数据获取
    public void getAllItemData(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        DataModel ud = new DataModel();
        ud = allItemData(ud, request);
        writeJson(ud, response);
    }

    // getAllItemData
    // 获取所有商品数据的方法
    private static DataModel allItemData(DataModel ud, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        DBHelper db = new DBHelper();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        // 查总有多少条数据  select count(*) total from UserInformation;
        String totalSql = "select count(*) total from item ";
        List<Map<String, Object>> maps = db.select(totalSql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        String limitSql = "select * from item limit " + limit + " offset " + skip;
        List<Item> limitMaps = db.select(Item.class, limitSql);
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


    // 传到oss的方法
    public static boolean uploadUtil (HttpServletRequest request) throws Exception {
        DataModel pd = new DataModel();  // 写出图片传输的结果
        OssUtils ossUtils = new OssUtils();
        Part filePart = request.getPart("file");  // 拿到前端的图片数据
        String name = filePart.getSubmittedFileName();   // 提交的名字
        name = "item/picture" + name;
        InputStream inputStream = filePart.getInputStream();  // 输出流
        String itemOssUrl = "https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/";
        Boolean result = ossUtils.UploadImageToOSS( name, inputStream); // 上传到oss的结果
       return result;
    }


}
