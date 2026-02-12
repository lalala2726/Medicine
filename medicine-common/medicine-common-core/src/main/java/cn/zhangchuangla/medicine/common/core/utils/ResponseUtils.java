package cn.zhangchuangla.medicine.common.core.utils;

import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 响应工具类
 */
public class ResponseUtils {

    private static final Logger log = LoggerFactory.getLogger(ResponseUtils.class);

    /**
     * 异常消息返回(适用过滤器中处理异常响应)
     *
     * @param response   HttpServletResponse
     * @param resultCode 响应结果码
     */
    public static void writeErrMsg(HttpServletResponse response, ResponseCode resultCode, HttpStatus httpStatus) {
        writeErrMsg(response, httpStatus, resultCode, null);
    }

    public static void writeErrMsg(HttpServletResponse response, HttpStatus httpStatus, String message) {
        writeErrMsg(response, httpStatus, null, message);
    }

    /**
     * 响应消息返回(适用过滤器中处理异常响应)
     *
     * @param response   HttpServletResponse
     * @param resultCode 响应结果码
     * @param message    自定义错误消息
     */
    public static void writeErrMsg(HttpServletResponse response, HttpStatus httpStatus, ResponseCode resultCode, String message) {
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (PrintWriter writer = response.getWriter()) {
            String jsonResponse;
            if (resultCode != null && message != null) {
                jsonResponse = JSONUtils.toJson(AjaxResult.error(resultCode, message));
            } else if (resultCode != null) {
                jsonResponse = JSONUtils.toJson(AjaxResult.error(resultCode));
            } else {
                jsonResponse = JSONUtils.toJson(AjaxResult.error(message));
            }
            writer.print(jsonResponse);
            writer.flush(); // 确保将响应内容写入到输出流
        } catch (IOException e) {
            log.error("响应异常处理失败", e);
        }
    }

    /**
     * 异常消息返回(适用过滤器中处理异常响应)
     *
     * @param response   HttpServletResponse
     * @param resultCode 响应结果码
     * @param message    自定义错误消息
     */
    public static void writeErrMsg(HttpServletResponse response, ResponseCode resultCode, HttpStatus httpStatus, String message) {
        writeErrMsg(response, httpStatus, resultCode, message);
    }


}
