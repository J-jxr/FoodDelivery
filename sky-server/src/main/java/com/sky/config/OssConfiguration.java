package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 工具类的配置类。
 * 该类用于将 AliOssUtil 工具类注册为 Spring 容器中的一个 Bean，
 * 并通过 AliOssProperties 自动注入配置文件中的相关参数。
 */
@Configuration // 表明这是一个配置类，用于定义 Bean 和相关配置
@Slf4j // Lombok 注解，自动生成日志对象 log，用于记录日志信息
public class OssConfiguration {

    /**
     * 定义 AliOssUtil 的 Bean，依赖 AliOssProperties 中的配置参数。
     *
     * @param aliOssProperties 自动注入的阿里云 OSS 配置属性
     * @return AliOssUtil 工具类的实例
     */
    @Bean // 表示将此方法的返回值注册为 Spring 容器中的一个 Bean，并且将该 Bean 的作用范围设置为单例（singleton）
    @ConditionalOnMissingBean // 表示如果容器中不存在 AliOssUtil 类型的 Bean，则创建该 Bean，保持IOC容器中肯定有这么一个Bean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        // 输出日志，显示当前注入的配置参数
        log.info("开始创建阿里云文件上传工具类对象：{}", aliOssProperties);

        // 使用 AliOssProperties 中的属性创建 AliOssUtil 对象并返回
        return new AliOssUtil(
                aliOssProperties.getEndpoint(), // 阿里云 OSS 的访问域名
                aliOssProperties.getAccessKeyId(), // AccessKey ID
                aliOssProperties.getAccessKeySecret(), // AccessKey Secret
                aliOssProperties.getBucketName() // OSS 存储桶名称
        );
    }
}
