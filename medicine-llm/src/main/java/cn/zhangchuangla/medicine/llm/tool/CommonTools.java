package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.enums.EventType;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import lombok.RequiredArgsConstructor;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

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
@RequiredArgsConstructor
public class CommonTools {

    private final SseMessageInjector messageInjector;


    /**
     * 通用表达式计算器，支持加减乘除、括号、幂运算以及 exp4j 内置函数（如 sqrt、abs、sin、cos 等）。
     * 使用示例：
     * 1) 直接计算：expression = "3*(2+5)/sqrt(4)"，variables 为空或不填。
     * 2) 带变量：expression = "(principal * rate) / 12"，variables 传 {"principal": 10000, "rate": 0.045}。
     */
    @Tool(name = "calculator_eval", description = "通用表达式计算器，支持加减乘除、括号、幂运算以及内置函数")
    public double evaluateExpression(
            @ToolParam(description = "需要计算的数学表达式，如 3*(2+5)/sqrt(4)") String expression,
            @ToolParam(description = "可选的变量映射，例如 {\"x\": 2, \"y\": 3}") Map<String, Double> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }

        messageInjector.callToolAction(EventType.TOOL_CALL_START, "正在计算中");
        try {
            ExpressionBuilder builder = new ExpressionBuilder(expression);
            if (variables != null && !variables.isEmpty()) {
                builder = builder.variables(variables.keySet());
            }

            Expression exp = builder.build();
            if (variables != null && !variables.isEmpty()) {
                variables.forEach(exp::setVariable);
            }

            double result = exp.evaluate();
            messageInjector.callToolAction(EventType.TOOL_CALL_END, "计算完成");
            return result;
        } catch (IllegalArgumentException e) {
            messageInjector.callToolAction(EventType.TOOL_CALL_END, "通用表达式计算失败");
            throw new IllegalArgumentException("表达式不合法或变量缺失: " + e.getMessage(), e);
        }
    }


    @Tool(name = "get_current_time", description = "获取当前精确时间，包含日期、时间和时区信息")
    public Map<String, String> getCurrentTime() {
        messageInjector.callToolAction(EventType.TOOL_CALL_START, "正在获取当前时间");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        Map<String, String> result = Map.of(
                "iso_format", now.toString(),
                "formatted", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "day_of_week", now.getDayOfWeek().toString(),
                "zone", "Asia/Shanghai"
        );
        messageInjector.callToolAction(EventType.TOOL_CALL_END, "当前时间获取完成");
        return result;
    }

    @Tool(name = "calculate_date_difference", description = "计算两个日期之间的天数差")
    public long calculateDateDifference(
            @ToolParam(description = "开始日期，格式 yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式 yyyy-MM-dd") String endDate) {
        try {
            messageInjector.callToolAction(EventType.TOOL_CALL_START, "正在计算日期差");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime end = LocalDateTime.parse(endDate + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            long days = Duration.between(start, end).toDays();
            messageInjector.callToolAction(EventType.TOOL_CALL_END, "日期差计算完成");
            return days;
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }
}
