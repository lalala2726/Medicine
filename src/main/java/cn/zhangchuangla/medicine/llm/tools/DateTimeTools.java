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
    String getCurrentTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        return String.valueOf(currentTimeMillis);
    }

    /**
     * 获取当前日期时间（标准格式）
     *
     * @return 格式化的日期时间字符串，如 "2025-09-05 19:15:30"
     */
    @Tool(name = "getCurrentDateTime", description = "Get current date and time in standard format")
    String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DATETIME_FORMATTER);
    }

    /**
     * 获取当前日期
     *
     * @return 格式化的日期字符串，如 "2025-09-05"
     */
    @Tool(name = "getCurrentDate", description = "Get current date in yyyy-MM-dd format")
    String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DATE_FORMATTER);
    }

    /**
     * 获取当前时间
     *
     * @return 格式化的时间字符串，如 "19:15:30"
     */
    @Tool(name = "getCurrentTime", description = "Get current time in HH:mm:ss format")
    String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(TIME_FORMATTER);
    }

    /**
     * 获取当前ISO 8601格式日期时间（包含时区）
     *
     * @return ISO 8601格式的时间字符串，如 "2025-09-05T19:15:30+08:00"
     */
    @Tool(name = "getIsoDateTime", description = "Get current date and time in ISO 8601 format with timezone")
    String getIsoDateTime() {
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
    String getTimestampFromDateTime(String dateTimeStr) {
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
    String getDateTimeFromTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault()
            );
            return dateTime.format(DATETIME_FORMATTER);
        } catch (Exception e) {
            return "Error: Invalid timestamp format";
        }
    }

    /**
     * 计算两个时间戳之间的时间差（小时）
     *
     * @param timestamp1Str 第一个时间戳字符串
     * @param timestamp2Str 第二个时间戳字符串
     * @return 时间差的小时数（绝对值）
     */
    @Tool(name = "getTimeDifference", description = "Calculate time difference between two timestamps in hours")
    String getTimeDifference(String timestamp1Str, String timestamp2Str) {
        try {
            long timestamp1 = Long.parseLong(timestamp1Str);
            long timestamp2 = Long.parseLong(timestamp2Str);
            long diffHours = Math.abs(timestamp2 - timestamp1) / (1000 * 60 * 60);
            return String.valueOf(diffHours);
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
    String getTimezone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * 获取指定时区的当前日期时间
     *
     * @param timezone 时区ID，如 "Asia/Shanghai"、"UTC"、"America/New_York"
     * @return 指定时区的日期时间字符串，如 "2025-09-05 19:15:30"
     */
    @Tool(name = "getDateTimeInTimezone", description = "Get current date time in specific timezone")
    String getDateTimeInTimezone(String timezone) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
            return now.format(DATETIME_FORMATTER);
        } catch (Exception e) {
            return "Error: Invalid timezone. Please use valid timezone IDs like 'Asia/Shanghai', 'UTC', etc.";
        }
    }

    /**
     * 在当前日期时间基础上增加指定小时数
     *
     * @param hours 要增加的小时数（可为负数）
     * @return 增加小时后的日期时间字符串，如 "2025-09-05 22:15:30"
     */
    @Tool(name = "addHoursToDateTime", description = "Add hours to current date time. Format: yyyy-MM-dd HH:mm:ss")
    String addHoursToDateTime(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = now.plusHours(hours);
        return result.format(DATETIME_FORMATTER);
    }

    /**
     * 在当前日期时间基础上增加指定天数
     *
     * @param days 要增加的天数（可为负数）
     * @return 增加天数后的日期时间字符串，如 "2025-09-06 19:15:30"
     */
    @Tool(name = "addDaysToDateTime", description = "Add days to current date time. Format: yyyy-MM-dd HH:mm:ss")
    String addDaysToDateTime(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = now.plusDays(days);
        return result.format(DATETIME_FORMATTER);
    }

    /**
     * 判断今天是否是周末
     *
     * @return "true"表示是周末，"false"表示不是周末
     */
    @Tool(name = "isWeekend", description = "Check if today is weekend")
    String isWeekend() {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();
        return String.valueOf(dayOfWeek == 6 || dayOfWeek == 7);
    }

    /**
     * 获取当前是星期几
     *
     * @return 星期几的数字表示（1-7，1表示星期一，7表示星期日）
     */
    @Tool(name = "getDayOfWeek", description = "Get current day of week (1-7, where 1 is Monday)")
    String getDayOfWeek() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getDayOfWeek().getValue());
    }

    /**
     * 获取当前月份的天数
     *
     * @return 当前月份的天数，如 "30"或"31"
     */
    @Tool(name = "getDaysInMonth", description = "Get number of days in current month")
    String getDaysInMonth() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getMonth().length(now.toLocalDate().isLeapYear()));
    }

    /**
     * 获取当前年份
     *
     * @return 当前年份，如 "2025"
     */
    @Tool(name = "getYear", description = "Get current year")
    String getYear() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getYear());
    }

    /**
     * 获取当前月份
     *
     * @return 当前月份（1-12），如 "9"表示9月
     */
    @Tool(name = "getMonth", description = "Get current month (1-12)")
    String getMonth() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getMonthValue());
    }

    /**
     * 获取当前日期（月份中的第几天）
     *
     * @return 当前日期，如 "5"表示5号
     */
    @Tool(name = "getDay", description = "Get current day of month")
    String getDay() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getDayOfMonth());
    }

    /**
     * 获取当前小时（24小时制）
     *
     * @return 当前小时（0-23），如 "19"表示晚上7点
     */
    @Tool(name = "getHour", description = "Get current hour (0-23)")
    String getHour() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getHour());
    }

    /**
     * 获取当前分钟
     *
     * @return 当前分钟（0-59），如 "15"表示15分
     */
    @Tool(name = "getMinute", description = "Get current minute (0-59)")
    String getMinute() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getMinute());
    }

    /**
     * 获取当前秒数
     *
     * @return 当前秒数（0-59），如 "30"表示30秒
     */
    @Tool(name = "getSecond", description = "Get current second (0-59)")
    String getSecond() {
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(now.getSecond());
    }

    /**
     * 自定义格式化日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串，格式为 "yyyy-MM-dd HH:mm:ss"
     * @param pattern     输出格式模式，如 "yyyy年MM月dd日 HH时mm分ss秒"
     * @return 格式化后的日期时间字符串
     */
    @Tool(name = "formatDateTime", description = "Format date time string. Input format: yyyy-MM-dd HH:mm:ss, Output format: pattern")
    String formatDateTime(String dateTimeStr, String pattern) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (Exception e) {
            return "Error: Invalid date format or pattern. Input should be yyyy-MM-dd HH:mm:ss";
        }
    }

}
