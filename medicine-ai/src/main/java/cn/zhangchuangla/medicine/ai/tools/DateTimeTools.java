package cn.zhangchuangla.medicine.ai.tools;

import cn.zhangchuangla.medicine.ai.workflow.progress.WorkflowProgressContextHolder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 提供各种日期时间相关的工具方法，供AI调用
 *
 * @author Chuang
 * @since 2025/9/5 19:15
 */
@Component
public class DateTimeTools {
    /**
     * 日期格式化器：yyyy-MM-dd
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * 时间格式化器：HH:mm:ss
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    /**
     * 日期时间格式化器：yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void notifyInvoke(String toolName, String message) {
        WorkflowProgressContextHolder.publishToolInvoke(toolName, message);
    }

    private void notifyInvoke(String toolName) {
        notifyInvoke(toolName, "正在调用 " + toolName + " 工具");
    }

    private void notifyResult(String toolName, String result) {
        WorkflowProgressContextHolder.publishToolResult(toolName, result);
    }

    /**
     * 获取当前Unix时间戳（毫秒）
     *
     * @return 当前时间戳字符串，如 "1725532800000"
     */
    @Tool(name = "getCurrentTimestamp", description = "Get current Unix timestamp in milliseconds")
    public String getCurrentTimestamp() {
        final String tool = "getCurrentTimestamp";
        notifyInvoke(tool);
        String result = String.valueOf(System.currentTimeMillis());
        notifyResult(tool, result);
        return result;
    }

    /**
     * 获取当前日期时间（标准格式）
     *
     * @return 格式化的日期时间字符串，如 "2025-09-05 19:15:30"
     */
    @Tool(name = "getCurrentDateTime", description = "Get current date and time in standard format")
    public String getCurrentDateTime() {
        final String tool = "getCurrentDateTime";
        notifyInvoke(tool);
        String result = LocalDateTime.now().format(DATETIME_FORMATTER);
        notifyResult(tool, result);
        return result;
    }

    /**
     * 获取当前日期
     *
     * @return 格式化的日期字符串，如 "2025-09-05"
     */
    @Tool(name = "getCurrentDate", description = "Get current date in yyyy-MM-dd format")
    public String getCurrentDate() {
        final String tool = "getCurrentDate";
        notifyInvoke(tool);
        String result = LocalDateTime.now().format(DATE_FORMATTER);
        notifyResult(tool, result);
        return result;
    }

    /**
     * 获取当前时间
     *
     * @return 格式化的时间字符串，如 "19:15:30"
     */
    @Tool(name = "getCurrentTime", description = "Get current time in HH:mm:ss format")
    public String getCurrentTime() {
        final String tool = "getCurrentTime";
        notifyInvoke(tool);
        String result = LocalDateTime.now().format(TIME_FORMATTER);
        notifyResult(tool, result);
        return result;
    }

}
