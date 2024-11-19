package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

/**
 * 阿里云 OSS 工具类，用于文件上传到阿里云 OSS。
 */
@Data // Lombok 注解，自动生成 getter、setter、toString、equals 和 hashCode 方法
@AllArgsConstructor // Lombok 注解，自动生成全参构造器
@Slf4j // Lombok 注解，生成 log 对象，用于记录日志
public class AliOssUtil {

    // 阿里云 OSS 的访问域名或地址，例如：https://oss-cn-shanghai.aliyuncs.com
    private String endpoint;

    // 阿里云账号的 AccessKey ID，用户身份的标识，用于鉴权
    private String accessKeyId;

    // 阿里云账号的 AccessKey Secret，用户身份的密钥，用于鉴权
    private String accessKeySecret;

    // 阿里云 OSS 上的存储桶名称，指定文件存储的具体位置
    private String bucketName;

    /**
     * 上传文件到阿里云 OSS。
     *
     * @param bytes      文件的字节数组
     * @param objectName 文件在 OSS 上的对象名称（包括路径）
     * @return 文件的访问 URL
     */
    public String upload(byte[] bytes, String objectName) {
        // 创建 OSS 客户端实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 调用 putObject 方法上传文件
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            // 捕获 OSS 服务端异常
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage()); // 错误信息
            System.out.println("Error Code:" + oe.getErrorCode()); // 错误码
            System.out.println("Request ID:" + oe.getRequestId()); // 请求 ID
            System.out.println("Host ID:" + oe.getHostId()); // 主机 ID
        } catch (ClientException ce) {
            // 捕获客户端异常
            System.out.println("Caught a ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage()); // 客户端错误信息
        } finally {
            // 确保释放客户端资源
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 根据规则生成文件访问路径，格式为：https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName) // 添加存储桶名称
                .append(".") // 添加分隔符
                .append(endpoint) // 添加 OSS 服务地址
                .append("/") // 添加路径分隔符
                .append(objectName); // 添加文件对象名称

        // 记录日志，输出文件上传后的访问路径
        log.info("文件上传到:{}", stringBuilder.toString());

        // 返回文件的访问 URL
        return stringBuilder.toString();
    }
}
