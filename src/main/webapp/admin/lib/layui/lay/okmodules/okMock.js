"use strict";
layui.define([], function (exprots) {
    let okMock = {
        api: {
            login: "https://www.easy-mock.com/mock/5d5d0dd46cfcbd1b8627bf1d/ok-admin-v2.0/login",
            bsgrid: "http://rap2api.taobao.org/app/mock/233041/bsgrid",
            datatables: "http://rap2api.taobao.org/app/mock/233041/datatables",

            // 用户列表页面加载的用户数据
            // listUser: "http://rap2api.taobao.org/app/mock/233041/user/listUser",
            listUser: "../../user.action?op=getAllUserData",

            // ‘所有订单’页面的数据
            alltRade:"../../order.action?op=getAllOrderData",

            // ‘发货处理’页面的数据
            shippingOorder:"../../order.action?op=getAllUnshippedOrderData",

            // ‘退货处理’页面的数据 getAllBackOrderData
            orderBack:"../../order.action?op=getAllBackOrderData",

            // ‘垃圾回收’页面的数据
            removedOorder:"../../order.action?op=getAllrRmovedOorderData",

            // 销售统计页面的数据
            saleData:"../../finance.action?op=getSaleData",


            listRole: "http://rap2api.taobao.org/app/mock/233041/role/listRole",
            listArticle: "http://rap2api.taobao.org/app/mock/233041/article/listArticle",
            listMessage: "http://rap2api.taobao.org/app/mock/233041/message/listMessage",

            // 产品列表/商品管理/商品评价 的数据来源
            // listProduct: "http://rap2api.taobao.org/app/mock/233041/product/listProduct",
            listProduct: "../../item.action?op=getAllItemData",

            // 体验详情
            listComment: "../../comment.action?op=getAllComment",

            // 库存管理的数据
            listStock: "../../inventory.action?op=getAllStockData",

            listlack: "../../inventory.action?op=getAllLackStockData",





            listDownload: "http://rap2api.taobao.org/app/mock/233041/download/listDownload",
            listLink: "http://rap2api.taobao.org/app/mock/233041/link/listLink",
            listTask: "http://rap2api.taobao.org/app/mock/233041/task/listTask",
            listImage: "http://rap2api.taobao.org/app/mock/233041/image/listImage",
            listBbs: "http://rap2api.taobao.org/app/mock/233041/bbs/listBbs",


            menu: {
                list: "https://easy-mock.com/mock/5d0ce725424f15399a6c2068/okadmin/menu/list"
            },
            user: {
                list: "http://rap2api.taobao.org/app/mock/233041/user/list",
            },
            role: {
                list: "http://rap2api.taobao.org/app/mock/233041/role/list"
            },
            permission: {
                list: "http://rap2api.taobao.org/app/mock/233041/permission/list",
            },
            article: {
                // 数据字典的数据
                list: "../../frontEdit.action?op=getAllSystemInfo",
            },
            task: {
                list: "http://rap2api.taobao.org/app/mock/233041/task/list"
            },
            link: {
                list: "http://rap2api.taobao.org/app/mock/233041/link/list"
            },
            product: {
                list: "http://rap2api.taobao.org/app/mock/233041/product/list"
            },
            log: {
                list: "https://easy-mock.com/mock/5d0ce725424f15399a6c2068/okadmin/log/list"
            },
            message: {
                list: "http://rap2api.taobao.org/app/mock/233041/message/list"
            },
            download: {
                list: "http://rap2api.taobao.org/app/mock/233041/download/list"
            },
            bbs: {
                list: "http://rap2api.taobao.org/app/mock/233041/bbs/list"
            }
        }
    };
    exprots("okMock", okMock);
});
