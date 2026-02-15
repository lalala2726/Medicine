package cn.zhangchuangla.medicine.agent.config.condition;

import cn.zhangchuangla.medicine.agent.spi.AgentSpiLoader;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 判断 Agent SPI 是否存在实现。
 */
public class AgentSpiAvailableCondition implements Condition {

    @Override
    public boolean matches(@NotNull ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnAgentSpi.class.getName());
        if (attributes == null) {
            return false;
        }
        Object type = attributes.get("value");
        if (!(type instanceof Class<?> spiType)) {
            return false;
        }
        return AgentSpiLoader.hasImplementation(spiType);
    }
}
