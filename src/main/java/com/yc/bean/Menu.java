package com.yc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    private String title;   // 菜单名
    private String href;    // 相对地址
    private String fontFamily;  // 字体吧
    private String icon;
    private Boolean spread;   // 下拉展开吗
    private Boolean isCheck;
    private List<Menu> children;
}
