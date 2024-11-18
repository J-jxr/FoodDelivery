package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，标识某个方法字段自动填充
 *
 * 这个注解的作用是标记方法，告诉程序在执行某个方法时需要自动填充数据库中的字段。
 * 例如：当插入或更新数据时，自动填充创建时间、更新时间、操作人等字段。
 *
 * 这个注解主要与数据库操作（插入或更新）相关，目的是简化代码并保证操作的一致性。
 */
@Target(ElementType.METHOD)  // 表示该注解可以应用于方法上
@Retention(RetentionPolicy.RUNTIME)  // 表示注解在运行时可以被反射访问
public @interface AutoFill {
    /**
     * 操作类型：INSERT 或 UPDATE
     * 这个值用于标识当前方法是执行插入操作还是更新操作
     * @return 操作类型
     */
    OperationType value();  // 定义一个值，指定操作类型
}
