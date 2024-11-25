package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * UserLoginVO 是一个数据传输对象（Value Object），用于封装用户登录的相关信息。
 * 它实现了 Serializable 接口，支持序列化，便于在分布式系统中传输。
 */
@Data // Lombok 注解，自动生成 getter、setter、toString、equals 和 hashCode 方法
@Builder // Lombok 注解，提供构建者模式，便于灵活创建对象
@NoArgsConstructor // Lombok 注解，生成一个无参构造方法
@AllArgsConstructor // Lombok 注解，生成一个包含所有字段的全参构造方法
public class UserLoginVO implements Serializable {

    private static final long serialVersionUID = 1L; // 序列化版本号，用于保证对象序列化的兼容性

    /**
     * 用户的唯一标识符
     */
    private Long id;

    /**
     * 用户的微信 OpenID，表示微信用户的唯一标识符
     */
    private String openid;

    /**
     * 用户登录后分配的 JWT 令牌，用于身份认证
     */
    private String token;

}
