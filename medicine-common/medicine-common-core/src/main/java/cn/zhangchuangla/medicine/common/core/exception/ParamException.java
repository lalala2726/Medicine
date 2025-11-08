package cn.zhangchuangla.medicine.common.core.exception;

import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import lombok.Getter;

/**
 * @author Chuang
 * <p>
 * created on 2025/1/12 
 */
@Getter
public class ParamException extends RuntimeException {

    private final Integer code;

    public ParamException(String message) {
        super(message);
        this.code = ResponseResultCode.PARAM_ERROR.getCode();
    }

    public ParamException(ResponseResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public ParamException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ParamException(ResponseResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }


}
