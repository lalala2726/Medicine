package cn.zhangchuangla.medicine.llm.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

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
    /**
     * ISO 8601格式化器
     */
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * 获取当前Unix时间戳（毫秒）
     *
     * @return 当前时间戳字符串，如 "1725532800000"
     */
    @Tool(name = "getCurrentTimestamp", description = "Get current Unix timestamp in milliseconds")
    public String getCurrentTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        return String.valueOf(currentTimeMillis);
    }

    /**
     * 获取当前日期时间（标准格式）
     *
     * @return 格式化的日期时间字符串，如 "2025-09-05 19:15:30"
     */
    @Tool(name = "getCurrentDateTime", description = "Get current date and time in standard format")
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DATETIME_FORMATTER);
    }

    /**
     * 获取当前日期
     *
     * @return 格式化的日期字符串，如 "2025-09-05"
     */
    @Tool(name = "getCurrentDate", description = "Get current date in yyyy-MM-dd format")
    public String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DATE_FORMATTER);
    }

    /**
     * 获取当前时间
     *
     * @return 格式化的时间字符串，如 "19:15:30"
     */
    @Tool(name = "getCurrentTime", description = "Get current time in HH:mm:ss format")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(TIME_FORMATTER);
    }

    /**
     * 获取当前ISO 8601格式日期时间（包含时区）
     *
     * @return ISO 8601格式的时间字符串，如 "2025-09-05T19:15:30+08:00"
     */
    @Tool(name = "getIsoDateTime", description = "Get current date and time in ISO 8601 format with timezone")
    public String getIsoDateTime() {
        ZonedDateTime now = ZonedDateTime.now();
        return now.format(ISO_FORMATTER);
    }

    /**
     * 将日期时间字符串转换为时间戳
     *
     * @param dateTimeStr 日期时间字符串，格式为 "yyyy-MM-dd HH:mm:ss"
     * @return 转换后的时间戳字符串，如 "1725532800000"
     */
    @Tool(name = "getTimestampFromDateTime", description = "Convert date time string to timestamp. Format: yyyy-MM-dd HH:mm:ss")
    public String getTimestampFromDateTime(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
            return String.valueOf(dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } catch (Exception e) {
            return "Error: Invalid date format. Please use yyyy-MM-dd HH:mm:ss";
        }
    }

    /**
     * 将时间戳转换为日期时间字符串
     *
     * @param timestampStr 时间戳字符串
     * @return 格式化的日期时间字符串，如 "2025-09-05 19:15:30"
     */
    @Tool(name = "getDateTimeFromTimestamp", description = "Convert timestamp to date time string")
    public String getDateTimeFromTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault());
            return dateTime.format(DATETIME_FORMATTER);
        } catch (Exception e) {
            return "Error: Invalid timestamp format";
        }
    }

    /**
     * 获取当前系统时区
     *
     * @return 时区ID，如 "Asia/Shanghai"
     */
    @Tool(name = "getTimezone", description = "Get current system timezone")
    public String getTimezone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * 获取指定时区的当前日期时间
     *
     * @param timezone 时区ID，如 "Asia/Shanghai"、"UTC"、"America/New_York"
     * @return 指定时区的日期时间字符串，如 "2025-09-05 19:15:30"
     */
    @Tool(name = "getDateTimeInTimezone", description = "Get current date time in specific timezone")
    public String getDateTimeInTimezone(String timezone) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
            return now.format(DATETIME_FORMATTER);
        } catch (Exception e) {
            return "Error: Invalid timezone. Please use valid timezone IDs like 'Asia/Shanghai', 'UTC', etc.";
        }
    }

    /**
     * 判断今天是否是周末
     *
     * @return "true"表示是周末，"false"表示不是周末
     */
    @Tool(name = "isWeekend", description = "Check if today is weekend")
    public String isWeekend() {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();
        return String.valueOf(dayOfWeek == 6 || dayOfWeek == 7);
    }

}
