package com.yc.dao;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 此DbProperties继承 自  Properties,所以它也是  Map,也是一个键值对
 * 但增的功能是 ,此DbProperties必须是单例
 */
public class DbProperties extends Properties {
    private static DbProperties instance;
    private DbProperties(){
        //读取配置文件
        InputStream iis= DbProperties.class.getClassLoader().getResourceAsStream("db.properties");
        //Properties类的load方法加载
        try {
            this.load(    iis );    //   this就是 DbProperties  对象，
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    public static DbProperties getInstance(){
        if(   instance==null){
            instance=new DbProperties();
        }



        return instance;
    }
}
