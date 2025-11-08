package cn.zhangchuangla.medicine.common.core.exception;

import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import lombok.Getter;

/**
 * 认证失败异常
 *
 * @author Chuang
 * <p>
 * created on 2025/1/11 
 */
@Getter
public final class AuthorizationException extends RuntimeException {

    /**
     * 状态码
     */
    private final Integer code;

    public AuthorizationException() {
        super(ResponseResultCode.AUTHORIZED.getMessage());
        this.code = ResponseResultCode.AUTHORIZED.getCode();
    }

    public AuthorizationException(ResponseResultCode responseResultCode, String message) {
        super(message);
        this.code = responseResultCode.getCode();
    }

    public AuthorizationException(ResponseResultCode responseResultCode) {
        super(responseResultCode.getMessage());
        this.code = responseResultCode.getCode();
    }

    public AuthorizationException(String message) {
        super(message);
        this.code = ResponseResultCode.AUTHORIZED.getCode();
    }

}
