package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐菜品关系实体类。
 * <p>
 * 该类表示套餐与菜品之间的关系。每个套餐可以包含多个菜品，而每个菜品都可能属于多个套餐。
 * <p>
 * 该类包含了与菜品相关的基本信息，如菜品名称、价格、份数等，同时还记录了该菜品所属的套餐 ID。
 * <p>
 * 主要用于在数据库中存储套餐与菜品的多对多关系表，常见于餐饮系统中的套餐管理功能。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L; // 序列化版本UID，确保对象序列化和反序列化的一致性

    private Long id; // 主键ID，唯一标识一条套餐菜品关系记录

    // 套餐ID，表示该菜品所属的套餐
    private Long setmealId;

    // 菜品ID，表示该菜品的唯一标识
    private Long dishId;

    // 菜品名称（冗余字段），菜品的名称，为了提高查询效率而冗余存储
    private String name;

    // 菜品原价，用于显示菜品的原始价格，可能与套餐价格不同
    private BigDecimal price;

    // 份数，表示该菜品在套餐中的数量（份数）
    private Integer copies;
}
