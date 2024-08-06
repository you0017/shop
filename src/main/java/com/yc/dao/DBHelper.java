package com.yc.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DBHelper {

    // 如何来获取一个Connection
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        DbProperties p = DbProperties.getInstance();
//        Class.forName("oracle.jdbc.driver.OracleDriver");
        Class.forName("com.mysql.cj.jdbc.Driver");
//        Connection con = DriverManager.getConnection(p.getProperty("oracleurl") ,
//                                                p.getProperty("oracleuname"),
//                                                p.getProperty("oraclepwd"));
        Connection con = DriverManager.getConnection(p.getProperty("mysqlurl"),
                p.getProperty("mysqluname"),
                p.getProperty("mysqlpwd"));
        return con;
    }

    //  设置参数的方法
    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }


    /**
     * 基于模板设计模式的查询方法
     *
     * @param rowMapper： 对一行结果集的处理，返回一个对应的对象
     * @param sql
     * @param params
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> List<T> select(RowMapper<T> rowMapper, String sql, Object... params) throws SQLException, ClassNotFoundException {
        List<T> list = new ArrayList<>();

        // 查询步骤的模板
        try (
                Connection con = getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql);
        ) {
            this.setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            int num = 0;
            while (rs.next()) {
                // 结果集的每一行的处理，由 RowMapper 接口的实现决定
                T t = rowMapper.mapRow(rs, num);
                num++;
                list.add(t);
            }
        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    /**
     * 封装（insert， update， delete）
     * sql：是要执行的 更新语句  这里面可能有 n 个 ？ 占位符，及对应的n个参数
     * Object...: 动态数组，长度不确定，这种参数只能加在一个方法参数列表的最后
     * 例：update emp set ename = ?, mgr = ? where empno = ?
     * params: '张三', '李四', '1101'
     */
    public int doUpdate(String sql, Object... params) {
        // 返回成功执行的条数
        int result = -1;
        try (
                Connection con = getConnection();  //获取连接
                PreparedStatement pstmt = con.prepareStatement(sql)
        ) {

//        问题一： ？对应的参数类型是什么， 这个类型是什么，则 setXxxx() ???
//          解决：将所有的参数类型指定为 Object， 则可以 setObject()
//        问题二：有多少个参数？？？ params到底有几个
//          params 是动态数组， 则有length
            setParams(pstmt, params);
            result = pstmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 方法名相同，参数不同  -> 重载
     * 查询返回值是一个List<T> T代表任意的类的对象
     * T类的标准JavaBean：属性封装，对外提供get/set
     *
     * @param c：代表   T 类的反射类的对象
     * @param sql：
     * @param params
     * @param <T>
     * @return
     */
    public <T> List<T> select(Class<T> c, String sql, Object... params) throws IllegalAccessException, InstantiationException, InvocationTargetException {
//        System.out.println(sql);
        // 1. sql, params => 查询得到数据表数据
        List result = new ArrayList<>();
        List<Map<String, Object>> list = this.select(sql, params);


        for (Map<String, Object> map : list) {
            T t = c.newInstance();  // 调用了这个T类的无参构造方法
            // 2. 将Map<String, Object> 转换成 T 对象
            //      a、循环map中所有的键值  entrySet()
            Set<Map.Entry<String, Object>> set = map.entrySet();
            Iterator<Map.Entry<String, Object>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1).toLowerCase();
                Object value = entry.getValue();
                if (value==null){
                    continue;
                }
                //System.out.println(value);
                // 拼接为 setXxx 方法的名字
                String methodName = "set" + key;
                //System.out.println(methodName);
                //      b、找出 set 方法
                Method setMethod = findSetMethod(methodName, c);
                //      c、激活这个方法，就是设置值进去
                Class parameterTypeClass = setMethod.getParameterTypes()[0];
                String parameterTypeName = parameterTypeClass.getName();

                if ("int".equals(parameterTypeName) || "java.lang.Integer".equals(parameterTypeName)) {
                    setMethod.invoke(t, Integer.parseInt(value.toString()));
                } else if ("double".equals(parameterTypeName) || "java.lang.Double".equals(parameterTypeName)) {
                    setMethod.invoke(t, Double.parseDouble(value.toString()));
                } else if ("short".equals(parameterTypeName) || "java.lang.Short".equals(parameterTypeName)) {
                    setMethod.invoke(t, Short.parseShort(value.toString()));
                } else if ("byte".equals(parameterTypeName) || "java.lang.Byte".equals(parameterTypeName)) {
                    setMethod.invoke(t, Byte.parseByte(value.toString()));
                } else if ("boolean".equals(parameterTypeName) || "java.lang.Boolean".equals(parameterTypeName)) {
                    setMethod.invoke(t, Boolean.parseBoolean(value.toString()));
                } else if ("float".equals(parameterTypeName) || "java.lang.Float".equals(parameterTypeName)) {
                    setMethod.invoke(t, Float.parseFloat(value.toString()));
                } else if ("long".equals(parameterTypeName) || "java.lang.Long".equals(parameterTypeName)) {
                    setMethod.invoke(t, Long.parseLong(value.toString()));
                } else if("java.time.LocalDateTime".equals(parameterTypeName)){
                    // 定义日期时间字符串
                    String dateTimeString = value.toString();

                    // 定义日期时间格式器
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                    // 将字符串转换为LocalDateTime
                    LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);

                    // 输出结果
                    //System.out.println("Converted LocalDateTime: " + localDateTime);
                    setMethod.invoke(t, localDateTime);
                }else{
                    setMethod.invoke(t, value.toString());
                }

            }

            // 3. 将 T 对象存在 List 中
            result.add(t);
        }
        return result;
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


//    private static ArrayList allSetMethods( Method[] methods ) {
//        ArrayList setMethods = new ArrayList();
//        for (Method method : methods) {
//
//        }
//    }


    public List<Map<String, Object>> select(String sql, Object... params) {
        List<Map<String, Object>> list = new ArrayList<>();  // 设置一个List集合
        try (
                Connection con = getConnection();  //获取连接
                PreparedStatement pstmt = con.prepareStatement(sql)  // 预编译语句对象
        ) {
            setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            // ResultSet 中有结果集中所有的信息
            ResultSetMetaData rsmd = rs.getMetaData(); // 结果集元数据  =》有多少个列， 每个列叫什么名字
            int columnCount = rsmd.getColumnCount(); // 列的数量


            // 循环结果集将数据放入map中,然后map放入List中
            while (rs.next()) {
                HashMap<String, Object> map = new HashMap<>(); // 创建一个map对象
                for (int i = 0; i < columnCount; i++) {
                    map.put(rsmd.getColumnName(i + 1), rs.getObject(i + 1)); // 存每一列
                }
                list.add(map);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;

    }


}
