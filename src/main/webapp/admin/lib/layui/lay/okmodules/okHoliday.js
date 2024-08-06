"use strict";
layui.define(["okUtils"], function (exprots) {
    let okUtils = layui.okUtils;

    var okHoliday = {
        getContent: function () {
            let dateStr = okUtils.dateFormat(new Date(), "yyyy-MM-dd");
            let content = "";
            content = "欢迎来到数码商城后台<br/>" +
                "在这里您可以查看<span style='color:#5cb85c'>系统公告</span>、" +
                "<span style='color:#5cb85c'>商品列表</span>、" +
                "<span style='color:#5cb85c'>销售记录</span>" +
                "等记录<br/>" +
                "若有更好的建议欢迎致电<span id='noticeQQ'>15673401363</span>期待您的意见";
            return content;
        }
    }

    exprots("okHoliday", okHoliday);
});
