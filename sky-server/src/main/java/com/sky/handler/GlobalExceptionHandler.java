package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());   //捕获到异常，返回一个Result对象，给前端页面返回一定的结果
    }
    //全局异常处理器，用于解决整个项目中可能出现的所有异常，并做出相应的响应
    /*
@ExceptionHandler：这个注解告诉 Spring，当抛出 BaseException 类型的异常时，调用 exceptionHandler 方法进行处理。你可以指定具体的异常类型（例如 BaseException），或者不指定参数，处理所有异常类型。

BaseException：BaseException 是你自定义的异常类。你可以根据需要定义自己的异常类来表示业务逻辑中的各种错误。

log.error(...)：你使用 log.error() 打印异常信息到日志中，ex.getMessage() 返回异常的详细信息。在生产环境中，记录异常的日志非常重要，可以帮助你追踪和排查问题。

Result.error(...)：返回一个 Result 对象，Result 是一个自定义的响应封装类，通常用于封装业务逻辑处理的返回值。Result.error(ex.getMessage()) 会构造一个错误的 Result 对象，通常包括错误码和错误信息。你可以根据需要调整 Result 类中的错误响应格式。
     */


    /**
     * 处理 SQL 异常
     * 该方法专门用于捕获和处理数据库操作中的 SQL 约束异常（如主键冲突、唯一键冲突等）。
     *
     * @param ex SQLIntegrityConstraintViolationException 异常对象，表示 SQL 约束被违反。
     * @return Result 封装的统一响应结果对象。
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        // 获取异常的详细信息（异常消息）
        // 例如，可能的异常消息为： "Duplicate entry 'jiangxinrun' for key 'employee.idx_username'"
        String message = ex.getMessage();

        // 检查异常消息中是否包含 "Duplicate entry" 字段，这表明是插入重复键值引起的异常
        if (message.contains("uplicate entry")) {
            // 将异常消息按照空格分割成多个部分，方便提取相关信息
            String[] split = message.split(" ");

            // 提取冲突的具体值（即重复的键值，例如 'jiangxinrun'）
            // 这里假设键值总是位于分割后数组的第 3 个位置（索引为 2）
            String username = split[2];

            // 构建错误信息，将冲突的键值与预定义的错误常量（如 "已存在"）结合生成具体提示
            String msg = username + MessageConstant.ALREADY_EXISTS;

            // 返回带有错误消息的统一响应对象
            return Result.error(msg);
        } else {
            // 如果异常消息不包含 "Duplicate entry"，说明是其他未知的 SQL 异常
            // 返回通用的未知错误提示信息
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }


}
