package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS（对象存储服务）配置属性类。
 * 该类用于封装配置文件中的阿里云 OSS 相关配置信息，方便在代码中注入使用。
 */
@Component // 标注为 Spring 容器中的一个组件，表示此类会被 Spring 扫描并管理
@ConfigurationProperties(prefix = "sky.alioss")
// 指定配置属性的前缀为 "sky.alioss"，
// Spring 会将配置文件（如 application.yml 或 application.properties）中以 "sky.alioss" 开头的属性注入到该类中。
@Data // Lombok 注解，自动生成 getter、setter、toString、equals 和 hashCode 方法
public class AliOssProperties {

    /**
     * 阿里云 OSS 服务的访问域名或地址，例如：https://oss-cn-shanghai.aliyuncs.com
     */
    private String endpoint;

    /**
     * 阿里云账号的 AccessKey ID，用户身份的标识，用于鉴权。
     */
    private String accessKeyId;

    /**
     * 阿里云账号的 AccessKey Secret，用户身份的密钥，用于鉴权。
     */
    private String accessKeySecret;

    /**
     * 阿里云 OSS 上的存储桶名称，指定文件存储的具体位置。
     */
    private String bucketName;

}
