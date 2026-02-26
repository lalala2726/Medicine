package cn.zhangchuangla.medicine.agent.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 编码与中文标签映射项。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentCodePair {

    /**
     * 编码值。
     */
    String code();

    /**
     * 编码对应中文标签。
     */
    String label();
}
