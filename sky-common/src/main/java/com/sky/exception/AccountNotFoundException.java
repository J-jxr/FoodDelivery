package com.sky.exception;

/**
 * 账号不存在异常
 */
//AccountNotFoundException，该异常继承了 。BaseException
public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException() {
    }

    public AccountNotFoundException(String msg) {
        super(msg);
    }

}
