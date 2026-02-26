package cn.zhangchuangla.medicine.agent.support;

import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析 Agent VO 的实体及字段语义描述。
 */
@Component
public class AgentVoDescriptionResolver {

    private final Map<Class<?>, Optional<Meta>> cache = new ConcurrentHashMap<>();

    /**
     * 解析指定类型的描述元数据。
     *
     * @param type 待解析类型
     * @return 元数据（未标注 {@link AgentVoDesc} 时返回 empty）
     */
    public Optional<Meta> resolve(Class<?> type) {
        if (type == null) {
            return Optional.empty();
        }
        Class<?> normalizedType = normalizeClass(type);
        return cache.computeIfAbsent(normalizedType, this::resolveInternal);
    }

    private Optional<Meta> resolveInternal(Class<?> type) {
        AgentVoDesc voDesc = type.getAnnotation(AgentVoDesc.class);
        if (voDesc == null) {
            return Optional.empty();
        }

        LinkedHashMap<String, String> fieldDescriptions = new LinkedHashMap<>();
        collectFieldDescriptions(type, "", fieldDescriptions, ConcurrentHashMap.newKeySet());

        Map<String, String> immutableDescriptions = Collections.unmodifiableMap(new LinkedHashMap<>(fieldDescriptions));
        return Optional.of(new Meta(voDesc.value(), immutableDescriptions));
    }

    private void collectFieldDescriptions(Class<?> type,
                                          String prefix,
                                          LinkedHashMap<String, String> result,
                                          Set<Class<?>> visiting) {
        Class<?> normalizedType = normalizeClass(type);
        if (!visiting.add(normalizedType)) {
            return;
        }

        for (Field field : getAllFields(normalizedType)) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();
            String currentPath = prefix.isBlank() ? fieldName : prefix + "." + fieldName;

            AgentFieldDesc fieldDesc = field.getAnnotation(AgentFieldDesc.class);
            if (fieldDesc != null && !fieldDesc.value().isBlank()) {
                result.put(currentPath, fieldDesc.value());
            }

            appendNestedDescriptions(field, currentPath, result, visiting);
        }

        visiting.remove(normalizedType);
    }

    private void appendNestedDescriptions(Field field,
                                          String currentPath,
                                          LinkedHashMap<String, String> result,
                                          Set<Class<?>> visiting) {
        Class<?> fieldType = normalizeClass(field.getType());
        if (isAgentVo(fieldType)) {
            collectFieldDescriptions(fieldType, currentPath, result, visiting);
            return;
        }

        if (fieldType.isArray()) {
            Class<?> componentType = normalizeClass(fieldType.getComponentType());
            if (isAgentVo(componentType)) {
                collectFieldDescriptions(componentType, currentPath + "[]", result, visiting);
            }
            return;
        }

        if (!Iterable.class.isAssignableFrom(fieldType)) {
            return;
        }

        Class<?> elementType = resolveCollectionElementType(field.getGenericType());
        if (elementType == null || !isAgentVo(elementType)) {
            return;
        }
        collectFieldDescriptions(elementType, currentPath + "[]", result, visiting);
    }

    private Class<?> resolveCollectionElementType(Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return null;
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return null;
        }
        return normalizeClass(resolveType(actualTypeArguments[0]));
    }

    private Class<?> resolveType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return resolveType(parameterizedType.getRawType());
        }
        if (type instanceof WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length > 0) {
                return resolveType(upperBounds[0]);
            }
            return null;
        }
        if (type instanceof TypeVariable<?> typeVariable) {
            Type[] bounds = typeVariable.getBounds();
            if (bounds.length > 0) {
                return resolveType(bounds[0]);
            }
            return null;
        }
        return null;
    }

    private boolean isAgentVo(Class<?> type) {
        return type != null && type.getAnnotation(AgentVoDesc.class) != null;
    }

    private List<Field> getAllFields(Class<?> type) {
        List<Class<?>> hierarchy = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            hierarchy.add(current);
        }
        Collections.reverse(hierarchy);

        List<Field> fields = new ArrayList<>();
        for (Class<?> current : hierarchy) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
        }
        return fields;
    }

    private Class<?> normalizeClass(Class<?> type) {
        if (type == null) {
            return null;
        }
        Class<?> current = type;
        while (current.getName().contains("$$") && current.getSuperclass() != null) {
            current = current.getSuperclass();
        }
        return current;
    }

    /**
     * Agent VO 描述元数据。
     *
     * @param entityDescription 实体说明
     * @param fieldDescriptions 字段说明映射
     */
    public record Meta(String entityDescription, Map<String, String> fieldDescriptions) {
    }
}
