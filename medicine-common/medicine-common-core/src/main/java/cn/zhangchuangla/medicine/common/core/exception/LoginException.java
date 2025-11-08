package cn.zhangchuangla.medicine.common.core.exception;

import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import lombok.Getter;

import static cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode.LOGIN_ERROR;


/**
 * 登录失败
 *
 * @author Chuang
 * <p>
 * created on 2025/7/27
 */
@Getter
public final class LoginException extends RuntimeException {

    /**
     * 状态码
     */
    private final Integer code;

    public LoginException() {
        super(LOGIN_ERROR.getMessage());
        this.code = LOGIN_ERROR.getCode();
    }

    public LoginException(ResponseResultCode resultCode, String message) {
        super(message);
        this.code = LOGIN_ERROR.getCode();
    }

    public LoginException(ResponseResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public LoginException(String message) {
        super(message);
        this.code = LOGIN_ERROR.getCode();
    }

}
