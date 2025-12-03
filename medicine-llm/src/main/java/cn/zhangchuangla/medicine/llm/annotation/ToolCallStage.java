package cn.zhangchuangla.medicine.llm.annotation;

import java.lang.annotation.*;

/**
 * 建议对工具耗时较长的工具方法添加此注解，以在调用前后自动推送 SSE 工具事件。
 * <p>
 * 标记在 {@link org.springframework.ai.tool.annotation.Tool} 方法上，用于在调用前后自动推送 SSE 工具事件。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolCallStage {

    /**
     * 调用前发送的描述，如果为空则自动使用工具描述/方法名。
     */
    String start() default "";

    /**
     * 调用结束发送的描述，如果为空则自动使用工具描述/方法名。
     */
    String end() default "";
}
