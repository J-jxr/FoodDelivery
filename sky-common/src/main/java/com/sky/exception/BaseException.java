package com.sky.exception;

/**
 * 业务异常
 */

//BaseException是一个我们自己定义的异常的父类异常
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }

}
