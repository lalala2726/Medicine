package cn.zhangchuangla.medicine.common.http.model;

import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
@Data
public class BaseResponse<T> {

    private int code;

    private String message;

    private T data;

    public boolean isSuccess() {
        return code == 200;
    }
}
