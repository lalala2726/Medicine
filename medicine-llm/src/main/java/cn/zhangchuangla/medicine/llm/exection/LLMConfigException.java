package cn.zhangchuangla.medicine.llm.exection;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import lombok.Getter;

/**
 * @author Chuang
 * <p>
 * created on 2025/1/11
 */
@Getter
public final class LLMConfigException extends LLMException {

    /**
     * 状态码
     */
    private final Integer code;


    public LLMConfigException(ResponseCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public LLMConfigException(ResponseCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public LLMConfigException(String message) {
        super(message);
        this.code = ResponseCode.ERROR.getCode();
    }

}
