package com.xju.lostandfound.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统内部异常"),
    unauthorized(401, "未登录或Token失效"),
    PARAM_ERROR(400, "参数错误"),
    USER_EXIST(4001, "用户已存在"),
    USER_NOT_LOGIN(4002, "用户不存在或密码错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}