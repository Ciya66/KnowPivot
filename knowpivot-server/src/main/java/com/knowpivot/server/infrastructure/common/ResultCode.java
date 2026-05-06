package com.knowpivot.server.infrastructure.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过多"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // Business error codes
    TOKEN_QUOTA_INSUFFICIENT(50001, "Token 配额不足"),
    BUSINESS_PROCESS_FAILED(50002, "业务处理失败"),
    MODEL_SERVICE_UNAVAILABLE(50301, "模型服务不可用");

    private final int code;
    private final String message;
}
