package com.yc.web.servlet.admin.admservlet;

import com.yc.web.model.DataModel;
import com.yc.web.servlet.admin.bean.AdminReturnOrder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/goodsback.action")
public class GoodsBackServlet extends BaseServlet {

    // 退款的操作
    public void over(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DataModel ud = new DataModel();
        String backType = request.getParameter("back_type");  // 退货类型 1为退款，2为退货退款
        String returnId = request.getParameter("return_id");   // 退货单号
        String returnQuantity = request.getParameter("return_quantity");  // 退货数量
        String realityBackMoney = request.getParameter("realityBackMoney");  // 实际退款金额
        String productId = request.getParameter("product_id");  // 商品id

        if ("仅退款".equals(backType) ) {  // 仅退款
            String returnOrderSql = "UPDATE returnorders SET return_status=4 WHERE return_id=?"; // 修改退货单状态为已退款
            int returnOrderResult = db.doUpdate(returnOrderSql, returnId);
            if (returnOrderResult >= 1) {
                ud.setCode(0);
                ud.setMsg("仅退款操作成功");
            } else {
                ud.setCode(1);
                ud.setMsg("仅退款操作失败");
            }
        } else if ("退货退款".equals(backType)){  // 退货退款
            String itemSql = "UPDATE item SET stock=stock+? WHERE id=?";   // 修改库存
            int itemResult = db.doUpdate(itemSql, returnQuantity, productId);
            if (itemResult >= 1) {
                String returnOrderSql = "UPDATE returnorders SET return_status=4 WHERE return_id=?"; // 修改退货单状态为已退款
                int returnOrderResult = db.doUpdate(returnOrderSql, returnId);
                if (returnOrderResult >= 1) {
                    ud.setCode(0);
                    ud.setMsg("退货退款操作成功");
                } else {
                    ud.setCode(1);
                    ud.setMsg("退货退款操作失败");
                }
            } else {
                ud.setCode(1);
                ud.setMsg("退货退款操作失败");
            }
        }else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        writeJson(ud, response);
    }


    // 不通过退货请求
    public void no(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DataModel ud = new DataModel();
        String return_id = request.getParameter("return_id");  // 退货单号
        String sql = "UPDATE returnorders SET return_status=5 WHERE return_id=?";
        int result = db.doUpdate(sql, return_id);
        if (result >= 1) {
            ud.setCode(0);
            ud.setMsg("操作成功");
        } else {
            ud.setCode(1);
            ud.setMsg("操作失败");
        }
        writeJson(ud, response);
    }

    // 通过退货申请
    public void ok(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DataModel ud = new DataModel();
        String return_id = request.getParameter("return_id");  // 退货单号
        String backType = request.getParameter("back_type");  // 退货类型
        if (2 == Integer.parseInt(backType)) {  // 退货退款
            String sql = "UPDATE returnorders SET return_status=2 WHERE return_id=?";
            int result = db.doUpdate(sql, return_id);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
            } else {
                ud.setCode(1);
                ud.setMsg("操作失败");
            }
        }
        if (1 == Integer.parseInt(backType)) {  // 退货
            // 这里还要加上退款的逻辑代码
            String sql = "UPDATE returnorders SET return_status=3 WHERE return_id=?";
            int result = db.doUpdate(sql, return_id);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
            } else {
                ud.setCode(1);
                ud.setMsg("操作失败");
            }
        }
        writeJson(ud, response);
    }


    //
    public void backGoods(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DataModel ud = new DataModel();
        String aaa = request.getParameter("AAA");  // 1为同意，2为不同意
//        String returnQuantity = request.getParameter("return_quantity");  // 退货数量
        String return_id = request.getParameter("return_id");  // 退货单号
//        String orderId = request.getParameter("order_id");  // 订单号
//        String productId = request.getParameter("product_id");   // 商品id
        if (aaa == "1") {   // 如果同意，把售后订单改为处理中    把订单详情表改为处理中
            String aaaSql = "UPDATE returnorders SET return_status=2 WHERE return_id=?";
            int result = db.doUpdate(aaaSql, return_id);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
                writeJson(ud, response);
                return;
            }
            ud.setCode(1);
            ud.setMsg("操作失败");
            writeJson(ud, response);
            return;
        }

        if (aaa == "2") {   // 如果不同意，把售后订单改为不予退货
            String aaaSql = "UPDATE returnorders SET return_status=5 WHERE return_id=?";
            int result = db.doUpdate(aaaSql, return_id);
            if (result >= 1) {
                ud.setCode(0);
                ud.setMsg("操作成功");
                writeJson(ud, response);
                return;
            }
            ud.setCode(1);
            ud.setMsg("操作成功");
            writeJson(ud, response);
            return;
        }
        ud.setCode(1);
        ud.setMsg("操作失败");
        writeJson(ud, response);
    }


    // 查看售后单号的数据    只查看审核通过的的售后单号
    public void getOnlyMoney(HttpServletRequest request, HttpServletResponse resposne) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        String dataSql = "SELECT r.tracking_company, r.product_id, r.return_id, r.tracking_number, r.order_id, r.product_name, r.refund_amount, u.name, r.return_date, r.back_type, " +
                " r.return_status, r.return_reason, r.return_quantity " +
                " FROM returnorders r " +
                " JOIN userinformation u ON r.customer_id = u.id " +
                " WHERE FIELD(r.return_status, 2, 3, 4) IN (1, 2, 3) " +
                " ORDER BY FIELD(r.return_status, 3, 2, 4) limit ? offset ? ";
        String totalsql = "SELECT count(*) as total FROM returnorders where return_status in (3, 4)";
        List<AdminReturnOrder> select = db.select(AdminReturnOrder.class, dataSql, limit, skip);
        List<Map<String, Object>> maps = db.select(totalsql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        if (select != null && select.size() > 0) {
            ud.setCode(0);
            ud.setMsg("查询成功");
            ud.setData(select);
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        writeJson(ud, resposne);
    }

    // 查所有申请退款的售后单号的数据  只查看待处理、不同意退货的单子
    public void getGoodsAndMoneyData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DataModel ud = new DataModel();
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page - 1);
        String dataSql = "SELECT  r.product_id, r.return_id, r.tracking_number, r.order_id, r.product_name, r.refund_amount, u.name, " +
                "r.return_date, r.back_type, r.return_status, r.return_reason,  r.return_quantity " +
                "FROM returnorders r JOIN userinformation u ON r.customer_id = u.id WHERE r.return_status in ( 1, 5) " +
                " ORDER BY r.return_status ASC limit ? offset ?";
        String totalsql = "SELECT count(*) as total FROM returnorders WHERE return_status in (1, 5)";
        List<AdminReturnOrder> select = db.select(AdminReturnOrder.class, dataSql, limit, skip);
        List<Map<String, Object>> maps = db.select(totalsql);
        int total = Integer.parseInt(maps.get(0).get("total").toString());
        if (select != null && select.size() > 0) {
            ud.setCode(0);
            ud.setMsg("查询成功");
            ud.setData(select);
            ud.setCount(total);
        } else {
            ud.setCode(1);
            ud.setMsg("无数据");
        }
        writeJson(ud, response);
    }
}
