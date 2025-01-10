package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口控制器，提供与公共功能相关的 API，例如文件上传。
 */
@RestController // 标注为 REST 控制器，所有方法默认返回 JSON 数据
@RequestMapping("/admin/common") // 定义此控制器的基础 URL 路径为 /admin/common
@Api(tags = "通用接口") // Swagger 注解，用于生成接口文档，描述为“通用接口”
@Slf4j // 使用 Lombok 自动生成日志对象 log，方便记录日志信息
public class CommonController {

    // 注入工具类，用于处理文件上传至阿里云 OSS 的逻辑
    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传接口
     *
     * @param file 接收前端上传的文件
     * @return 上传结果的统一封装对象，成功返回文件路径，失败返回错误信息
     */
    @PostMapping("upload") // 映射 HTTP POST 请求到该方法，路径为 /admin/common/upload
    @ApiOperation("文件上传") // Swagger 注解，描述该接口的功能为“文件上传”
    public Result<String> upload(MultipartFile file) {
        // 记录日志，输出上传文件的相关信息
        log.info("文件上传：{}", file);

        try {
            // 获取文件的原始文件名
            String originalFilename = file.getOriginalFilename();

            // 从文件名中提取文件后缀名（包括 .）     从原始文件名最后一个点开始截取
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 使用 UUID 生成唯一的文件名，防止文件重名
            String objectName = UUID.randomUUID() + extension;

            // 使用 AliOssUtil 工具类，将文件上传至阿里云 OSS，并返回文件的 URL 路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            // 返回成功的统一结果封装对象，包含文件的访问路径
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败()", e);
            // 捕获文件处理过程中的异常，并打印堆栈信息
            e.printStackTrace();
            // 返回失败的统一结果封装对象，包含错误提示信息
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
