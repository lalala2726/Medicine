package cn.zhangchuangla.medicine.common.base;

import cn.zhangchuangla.medicine.enums.ResponseResultCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用API响应结果封装类
 *
 * @author Chuang
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Tag(name = "通用API响应结果封装类", description = "通用API响应结果封装类")
public class ResponseResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    @Schema(description = "状态码", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer code;

    /**
     * 返回消息
     */
    @Schema(description = "返回消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    /**
     * 时间戳 (毫秒)
     */
    @Schema(description = "时间戳 (毫秒)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long timestamp;

    /**
     * 数据
     */
    @Schema(description = "数据")
    private T data;

    // --- 私有构造函数 ---

    /**
     * 基础构造函数，初始化核心字段
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     */
    private ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 通过 ResponseResultCode 枚举和数据构造
     *
     * @param responseResultCode ResponseResultCode 枚举实例
     * @param data       数据
     */
    private ResponseResult(ResponseResultCode responseResultCode, T data) {
        this(responseResultCode.getCode(), responseResultCode.getMessage(), data);
    }

    /**
     * 通过 ResponseResultCode 枚举构造 (无数据)
     *
     * @param responseResultCode ResponseResultCode 枚举实例
     */
    private ResponseResult(ResponseResultCode responseResultCode) {
        this(responseResultCode, null);
    }


    /**
     * 成功返回 (无消息，无数据)
     */
    @Schema(description = "成功返回 (无消息，无数据)")
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(ResponseResultCode.SUCCESS);
    }

    /**
     * 成功返回 (带自定义消息，无数据)
     *
     * @param message 自定义成功消息
     */
    public static <T> ResponseResult<T> success(String message) {
        return new ResponseResult<>(ResponseResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 成功返回 (使用默认消息，带数据)
     *
     * @param data 返回的数据
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(ResponseResultCode.SUCCESS, data);
    }

    /**
     * 成功返回 (带自定义消息和数据)
     *
     * @param message 自定义成功消息
     * @param data    返回的数据
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(ResponseResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回 (使用默认错误码和消息，无数据)
     */
    public static <T> ResponseResult<T> error() {
        return new ResponseResult<>(ResponseResultCode.ERROR);
    }

    public static <T> ResponseResult<T> error(String message, Integer code) {
        return new ResponseResult<>(code, message, null);
    }


    /**
     * 失败返回 (使用默认错误码，带自定义消息，无数据)
     *
     * @param message 自定义错误消息
     */
    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(ResponseResultCode.ERROR.getCode(), message, null);
    }

    /**
     * 失败返回 (使用指定的 ResponseResultCode，无数据)
     *
     * @param responseResultCode 响应码枚举
     */
    public static <T> ResponseResult<T> error(ResponseResultCode responseResultCode) {
        return new ResponseResult<>(responseResultCode);
    }

    /**
     * 失败返回 (使用指定的 ResponseResultCode 和自定义消息，无数据)
     *
     * @param responseResultCode 响应码枚举
     * @param message    自定义错误消息 (将覆盖枚举中的默认消息)
     */
    public static <T> ResponseResult<T> error(ResponseResultCode responseResultCode, String message) {
        return new ResponseResult<>(responseResultCode.getCode(), message, null);
    }

    /**
     * 失败返回 (使用指定的自定义错误码和自定义消息，无数据)
     *
     * @param code    自定义错误码
     * @param message 自定义错误消息
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    /**
     * 警告返回 (使用默认警告码，带自定义消息，无数据)
     *
     * @param message 自定义警告消息
     */
    public static <T> ResponseResult<T> warning(String message) {
        return new ResponseResult<>(ResponseResultCode.WARNING.getCode(), message, null);
    }

}
