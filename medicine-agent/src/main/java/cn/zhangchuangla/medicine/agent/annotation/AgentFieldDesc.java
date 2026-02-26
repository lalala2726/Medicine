package cn.zhangchuangla.medicine.agent.annotation;

import java.lang.annotation.*;

/**
 * 标注字段的语义说明。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentFieldDesc {

    /**
     * 字段说明文本。
     */
    String value();
}
