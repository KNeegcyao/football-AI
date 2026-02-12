package com.soccer.forum.common.exception;

import com.soccer.forum.common.enums.ServiceErrorCode;

/**
 * 业务异常
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Integer code;

    public ServiceException(String message) {
        super(message);
        this.code = 500;
    }

    public ServiceException(ServiceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public ServiceException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
