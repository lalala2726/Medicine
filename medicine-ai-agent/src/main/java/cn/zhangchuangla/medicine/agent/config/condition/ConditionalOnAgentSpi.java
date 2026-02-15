package cn.zhangchuangla.medicine.agent.config.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当指定 Agent SPI 有实现时才装配 Bean。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AgentSpiAvailableCondition.class)
public @interface ConditionalOnAgentSpi {

    /**
     * SPI 接口类型。
     */
    Class<?> value();
}
