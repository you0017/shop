package com.yc.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonModel implements Serializable {
    private Integer code ;//响应码  0表示失败   1表示成功
    private Object obj;
    private String error;
}
