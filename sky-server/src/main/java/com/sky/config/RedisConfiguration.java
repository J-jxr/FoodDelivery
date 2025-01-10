package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 这是一个 Redis 配置类，用于设置如何连接 Redis，并配置 Redis 操作的模板。
 * RedisTemplate 是 Spring 用来和 Redis 进行交互的工具类。
 */
@Configuration // 表示这个类是一个配置类，Spring 会自动加载它
@Slf4j         // 自动为类添加一个 log 对象，用于打印日志
public class RedisConfiguration {
/*
这样，Spring 会在应用启动时调用这个方法并自动配置 RedisTemplate。
之后，你可以在 Spring 的任何地方（比如服务类、控制器等）注入并使用它进行 Redis 操作。
 */
    /**
     * 创建并配置 RedisTemplate 对象，RedisTemplate 用来操作 Redis 数据库。
     * 我们需要通过 RedisTemplate 来对 Redis 执行各种操作，比如存取数据、删除数据等。
     *
     * @param redisConnectionFactory Redis 连接工厂，用来创建与 Redis 服务的连接
     * @return 配置好的 RedisTemplate 对象
     */
    @Bean  // 表示这个方法返回的 RedisTemplate 对象会被注册到 Spring 容器中作为一个 Bean，可以在其他地方注入使用。
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 通过参数 RedisConnectionFactory 获取 Redis 的连接信息，Spring 会自动注入连接工厂
        // RedisTemplate 用来执行与 Redis 的交互，执行类似存储数据、取数据等操作

        RedisTemplate redisTemplate = new RedisTemplate();  // 创建 RedisTemplate 实例

        // 设置 RedisTemplate 使用的 Redis 连接工厂，用来建立与 Redis 的连接
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 这里可以配置序列化方式，常见的是 StringRedisSerializer，用于将键值对转化为字符串
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;  // 返回配置好的 RedisTemplate 实例，Spring 会自动把它注册为 Bean
    }
}
