package com.soccer.forum.common.enums;

/**
 * 业务错误码枚举
 */
public enum ServiceErrorCode {
    
    // 用户相关 1000-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_ACCOUNT_LOCKED(1004, "账号已被锁定"),
    
    // 权限相关 2000-2999
    UNAUTHORIZED(2001, "未授权"),
    FORBIDDEN(2002, "权限不足"),
    TOKEN_INVALID(2003, "Token无效或已过期"),
    
    // 业务逻辑相关 3000-3999
    PARAM_ERROR(3001, "参数错误"),
    DATA_NOT_FOUND(3002, "数据不存在"),
    OPERATION_FAILED(3003, "操作失败"),
    
    // 帖子/评论相关 4000-4999
    POST_NOT_FOUND(4001, "帖子不存在"),
    COMMENT_NOT_FOUND(4002, "评论不存在"),
    ALREADY_LIKED(4003, "已点赞"),
    ALREADY_FAVORITED(4004, "已收藏"),
    
    // 系统相关 9000-9999
    SYSTEM_ERROR(9999, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ServiceErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
