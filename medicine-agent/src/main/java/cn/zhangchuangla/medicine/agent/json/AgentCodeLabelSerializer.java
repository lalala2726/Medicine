package cn.zhangchuangla.medicine.agent.json;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentCodePair;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Agent 编码中文标签序列化器。
 */
public class AgentCodeLabelSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private final AgentCodeLabel annotation;

    public AgentCodeLabelSerializer() {
        this.annotation = null;
    }

    public AgentCodeLabelSerializer(AgentCodeLabel annotation) {
        this.annotation = annotation;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        AgentCodeLabel codeLabel = resolveAnnotation(gen);
        if (codeLabel == null) {
            gen.writeObject(value);
            return;
        }

        Object rawValue = resolveRawValue(codeLabel, value, gen.currentValue());
        if (rawValue == null) {
            gen.writeNull();
            return;
        }

        String code = String.valueOf(rawValue);
        String description = findLabel(code, codeLabel);
        if (description == null && codeLabel.fallbackToSource()) {
            description = code;
        }
        writeCodeObject(gen, serializers, rawValue, description);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            AgentCodeLabel current = property.getAnnotation(AgentCodeLabel.class);
            if (current == null) {
                current = property.getContextAnnotation(AgentCodeLabel.class);
            }
            if (current != null) {
                return new AgentCodeLabelSerializer(current);
            }
        }
        return this;
    }

    private String findLabel(String code, AgentCodeLabel codeLabel) {
        AgentCodePair[] pairs = codeLabel.pairs();
        if (pairs != null) {
            for (AgentCodePair pair : pairs) {
                if (pair.code().equals(code)) {
                    return pair.label();
                }
            }
        }
        return AgentCodeLabelRegistry.getLabel(codeLabel.dictKey(), code);
    }

    private Object resolveRawValue(AgentCodeLabel codeLabel, Object currentFieldValue, Object bean) {
        String sourceField = codeLabel.source();
        if (sourceField == null || sourceField.isBlank()) {
            return currentFieldValue;
        }
        if (bean == null) {
            return null;
        }
        return readSourceValue(bean, sourceField);
    }

    private void writeCodeObject(JsonGenerator gen,
                                 SerializerProvider serializers,
                                 Object rawValue,
                                 String description) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("value");
        serializers.defaultSerializeValue(rawValue, gen);
        gen.writeFieldName("description");
        if (description == null) {
            gen.writeNull();
        } else {
            gen.writeString(description);
        }
        gen.writeEndObject();
    }

    private AgentCodeLabel resolveAnnotation(JsonGenerator gen) {
        if (annotation != null) {
            return annotation;
        }
        Object bean = gen.currentValue();
        if (bean == null) {
            return null;
        }
        String propertyName = gen.getOutputContext() == null ? null : gen.getOutputContext().getCurrentName();
        if (propertyName == null || propertyName.isBlank()) {
            return null;
        }
        return findAnnotation(bean.getClass(), propertyName);
    }

    private AgentCodeLabel findAnnotation(Class<?> beanClass, String propertyName) {
        Class<?> current = beanClass;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(propertyName);
                return field.getAnnotation(AgentCodeLabel.class);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private Object readSourceValue(Object bean, String sourceField) {
        Class<?> current = bean.getClass();
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(sourceField);
                field.trySetAccessible();
                return field.get(bean);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }
}
