package cn.zhangchuangla.medicine.agent.annotation;

import java.lang.annotation.*;

/**
 * 标注 VO 的实体语义说明。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentVoDesc {

    /**
     * 实体说明文本。
     */
    String value();
}
