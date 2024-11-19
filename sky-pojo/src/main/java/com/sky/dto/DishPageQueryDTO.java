package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    //根据菜品名称查询Dish表
    private String name;

    //根据菜品分类id查询Category表      因此涉及到多表查询
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}






