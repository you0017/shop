package com.yc.web.service;

import com.yc.bean.Item;
import com.yc.bean.PageBean;
import com.yc.dao.DBHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class PageService {

    private DBHelper db = new DBHelper();

    /**
     * 分页查询
     * @param pageBean
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public PageBean<Item> get(PageBean<Item> pageBean,String search,String sortby,String sort) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        int i = (pageBean.getPageno() - 1) * pageBean.getPagesize();
        String sql = "";
        //这个是按关键字搜索
        if (search != null && !search.equals("")){
            sql = "select * from (select * from item where (category like '%" + search + "%' or name like '%" + search + "%' or brand like '%"+ search +"%' or spec like '%" + search + "%') and status=1) a ";
        }else{
            sql = "select * from item where status=1 ";
        }
        if (sortby!=null && !sortby.equals("")){
            sql += "order by "+sortby+" ";
        }
        if (sort != null && !sort.equals("")){
            sql += " "+sort+" ";
        }
        sql += " limit ? offset ?";


        //分页查询的数据
        List<Item> select = db.select(Item.class, sql, pageBean.getPagesize(),i);
        pageBean.setDataset(select);

        //这个是按关键字搜索
        if (search != null && !search.equals("")){
            sql = "select count(*) num from item where (category like '%" + search + "%' or name like '%" + search + "%' or brand like '%"+ search +"%' or spec like '%" + search + "%') and status=1 ";
        }else{
            sql = "select count(*) num from item where status=1 ";
        }
        List<Map<String, Object>> count = db.select(sql);
        pageBean.setTotal((Long) count.get(0).get("num"));  //总数据量

        //计算页数
        pageBean.setTotalpages((int) (pageBean.getTotal() % pageBean.getPagesize() == 0 ? pageBean.getTotal() / pageBean.getPagesize() : pageBean.getTotal() / pageBean.getPagesize() + 1));
        //计算上一页
        if (pageBean.getPageno()==1 || pageBean.getTotalpages()==0){
            pageBean.setPre(1);
        }else{
            pageBean.setPre(pageBean.getPageno()-1);
        }

        //计算下一页
        if (pageBean.getPageno()==pageBean.getTotalpages() || pageBean.getTotalpages()==0){
            //最后一页
            pageBean.setNext(pageBean.getPageno());
        }else{
            pageBean.setNext(pageBean.getPageno()+1);
        }


        return pageBean;
    }
}
