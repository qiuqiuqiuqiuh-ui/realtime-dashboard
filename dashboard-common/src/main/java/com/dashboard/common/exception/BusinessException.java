package com.dashboard.common.exception;

import com.dashboard.common.result.ErrorCode;

/**
 * 业务异常
 *
 * 用于可预见的业务错误，如参数校验失败、资源不存在等。
 * 不同于系统异常 (NPE、IO 异常)，业务异常携带明确的错误码。
 */
public class BusinessException extends RuntimeException {

    private final int code;
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
    }

    public int getCode() { return code; }
    public ErrorCode getErrorCode() { return errorCode; }
}
