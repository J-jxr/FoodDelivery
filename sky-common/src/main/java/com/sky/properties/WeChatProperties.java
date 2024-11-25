package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WeChatProperties 类用于加载与微信小程序和微信支付相关的配置项。
 * 配置项来自 Spring Boot 配置文件，以 "sky.wechat" 为前缀。
 */
@Component // 将该类注册为 Spring 容器中的一个组件，便于依赖注入
@ConfigurationProperties(prefix = "sky.wechat")
// 从配置文件中加载以 "sky.wechat" 为前缀的配置项
@Data // Lombok 注解，自动生成 getter、setter、toString、equals 和 hashCode 方法
public class WeChatProperties {

    /**
     * 微信小程序的 App ID，用于标识特定的小程序。
     * 配置文件对应项：sky.wechat.appid
     */
    private String appid;

    /**
     * 微信小程序的秘钥（App Secret），用于接口调用时的身份验证。
     * 配置文件对应项：sky.wechat.secret
     */
    private String secret;

    /**
     * 微信支付商户号，用于标识商户账户。
     * 配置文件对应项：sky.wechat.mchid
     */
    private String mchid;

    /**
     * 商户 API 证书的证书序列号，用于标识 API 证书。
     * 配置文件对应项：sky.wechat.mch-serial-no
     */
    private String mchSerialNo;

    /**
     * 商户私钥文件路径，用于签名支付请求。
     * 配置文件对应项：sky.wechat.private-key-file-path
     */
    private String privateKeyFilePath;

    /**
     * API V3 密钥，用于证书解密和支付接口调用。
     * 配置文件对应项：sky.wechat.api-v3-key
     */
    private String apiV3Key;

    /**
     * 微信支付平台证书文件路径，用于验证支付平台返回的签名。
     * 配置文件对应项：sky.wechat.we-chat-pay-cert-file-path
     */
    private String weChatPayCertFilePath;

    /**
     * 支付成功的回调地址，微信支付完成后通知商户的接口地址。
     * 配置文件对应项：sky.wechat.notify-url
     */
    private String notifyUrl;

    /**
     * 退款成功的回调地址，微信退款完成后通知商户的接口地址。
     * 配置文件对应项：sky.wechat.refund-notify-url
     */
    private String refundNotifyUrl;

}
