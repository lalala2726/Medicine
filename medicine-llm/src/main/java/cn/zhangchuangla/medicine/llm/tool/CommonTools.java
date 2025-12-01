package cn.zhangchuangla.medicine.llm.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/1
 */
@Component
public class CommonTools {

    // ==================== 计算工具 ====================

    @Tool(name = "calculator_add", description = "加法计算")
    public double add(@ToolParam(description = "被加数") double a, @ToolParam(description = "加数") double b) {
        return BigDecimal.valueOf(a).add(BigDecimal.valueOf(b)).doubleValue();
    }

    @Tool(name = "calculator_subtract", description = "减法计算")
    public double subtract(@ToolParam(description = "被减数") double a, @ToolParam(description = "减数") double b) {
        return BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)).doubleValue();
    }

    @Tool(name = "calculator_multiply", description = "乘法计算")
    public double multiply(@ToolParam(description = "被乘数") double a, @ToolParam(description = "乘数") double b) {
        return BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b)).doubleValue();
    }

    @Tool(name = "calculator_divide", description = "除法计算")
    public double divide(@ToolParam(description = "被除数") double a, @ToolParam(description = "除数") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        return BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), 4, RoundingMode.HALF_UP).doubleValue();
    }


    @Tool(name = "get_current_time", description = "获取当前精确时间，包含日期、时间和时区信息")
    public Map<String, String> getCurrentTime() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        return Map.of(
                "iso_format", now.toString(),
                "formatted", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "day_of_week", now.getDayOfWeek().toString(),
                "zone", "Asia/Shanghai"
        );
    }

    @Tool(name = "calculate_date_difference", description = "计算两个日期之间的天数差")
    public long calculateDateDifference(
            @ToolParam(description = "开始日期，格式 yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式 yyyy-MM-dd") String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime end = LocalDateTime.parse(endDate + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Duration.between(start, end).toDays();
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }
}
