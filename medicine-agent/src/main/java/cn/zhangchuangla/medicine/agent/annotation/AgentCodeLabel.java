package cn.zhangchuangla.medicine.agent.annotation;

import cn.zhangchuangla.medicine.agent.json.AgentCodeLabelSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为编码字段生成中文标签字段。
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = AgentCodeLabelSerializer.class, nullsUsing = AgentCodeLabelSerializer.class)
public @interface AgentCodeLabel {

    /**
     * 来源编码字段名，例如 status、payType。
     */
    String source();

    /**
     * 字典注册表 key，用于大映射配置。
     */
    String dictKey() default "";

    /**
     * 注解内联小映射，优先于 dictKey。
     */
    AgentCodePair[] pairs() default {};

    /**
     * 未命中映射时是否回退为原编码值。
     */
    boolean fallbackToSource() default true;
}
