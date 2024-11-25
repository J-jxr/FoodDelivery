package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JwtProperties 类用于从配置文件中加载与 JWT（JSON Web Token）相关的配置信息。
 * 该类使用 Spring Boot 的 @ConfigurationProperties 注解来自动将配置文件中的属性映射到字段中。
 * 使用 @Component 注解将该类注册为 Spring 容器的一个组件，使其可以被依赖注入。
 */
@Component // 将 JwtProperties 注册为一个 Spring 组件，便于在项目中注入使用
@ConfigurationProperties(prefix = "sky.jwt")
// 将配置文件中以 "sky.jwt" 为前缀的配置项绑定到此类的字段
@Data // Lombok 提供的注解，自动生成 getter/setter、toString、equals 和 hashCode 方法
public class JwtProperties {

    /**
     * 管理端员工生成 JWT 令牌相关配置
     */
    private String adminSecretKey; // 管理端员工生成 JWT 令牌的密钥，用于签名和验证令牌的合法性
    private long adminTtl; // 管理端员工生成的 JWT 令牌的有效时长（单位：毫秒）
    private String adminTokenName; // 管理端员工的 JWT 令牌在请求头中的名称

    /**
     * 用户端微信用户生成 JWT 令牌相关配置
     */
    private String userSecretKey; // 用户端微信用户生成 JWT 令牌的密钥，用于签名和验证令牌的合法性
    private long userTtl; // 用户端微信用户生成的 JWT 令牌的有效时长（单位：毫秒）
    private String userTokenName; // 用户端微信用户的 JWT 令牌在请求头中的名称

}
