package com.yc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    /**
     * 由最终用户决定如何处理 ResultSet 中的第 rowNum 行
     * @param rs
     * @param rowNum
     * @return
     * @throws SQLException
     */

    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
