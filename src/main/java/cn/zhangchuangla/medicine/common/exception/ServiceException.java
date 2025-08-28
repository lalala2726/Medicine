package cn.zhangchuangla.medicine.common.exception;

import cn.zhangchuangla.medicine.enums.ResponseResultCode;
import lombok.Getter;

/**
 * @author Chuang
 * <p>
 * created on 2025/1/11 10:04
 */
@Getter
public final class ServiceException extends RuntimeException {

    /**
     * 状态码
     */
    private final Integer code;


    public ServiceException(ResponseResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public ServiceException(ResponseResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public ServiceException(String message) {
        super(message);
        this.code = ResponseResultCode.ERROR.getCode();
    }

}
