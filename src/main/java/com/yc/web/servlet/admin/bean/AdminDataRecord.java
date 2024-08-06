package com.yc.web.servlet.admin.bean;

import lombok.Data;

/**
 * 数据字典的类
 */
@Data
public class AdminDataRecord {
    private Integer id;
    private Integer recorde_status;
    private String recorde_name;
    private String recorde_value;
}
