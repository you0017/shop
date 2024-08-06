package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
// 数据字典
public class DataRecord implements Serializable {
    private Integer id;
    private String recorde_name;  // 键
    private String recorde_value;  // 值
    private Integer recorde_status; // 是否启用
}
