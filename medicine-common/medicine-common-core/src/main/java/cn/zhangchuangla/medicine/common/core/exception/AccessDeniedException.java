package cn.zhangchuangla.medicine.common.core.exception;

import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import lombok.Getter;

/**
 * 授权失败异常
 *
 * @author Chuang
 * <p>
 * created on 2025/7/27 23:03
 */
@Getter
public final class AccessDeniedException extends RuntimeException {

    /**
     * 状态码
     */
    private final Integer code;

    public AccessDeniedException() {
        super(ResponseResultCode.FORBIDDEN.getMessage());
        this.code = ResponseResultCode.FORBIDDEN.getCode();
    }

    public AccessDeniedException(String message) {
        super(message);
        this.code = ResponseResultCode.FORBIDDEN.getCode();
    }

    public AccessDeniedException(ResponseResultCode responseResultCode) {
        super(responseResultCode.getMessage());
        this.code = ResponseResultCode.FORBIDDEN.getCode();
    }


}
