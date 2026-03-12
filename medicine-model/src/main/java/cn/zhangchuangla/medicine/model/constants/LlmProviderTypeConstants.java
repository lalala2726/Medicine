package cn.zhangchuangla.medicine.model.constants;

import java.util.Set;

/**
 * 大模型提供商类型常量。
 */
public final class LlmProviderTypeConstants {

    public static final String OPENAI = "openai";

    public static final String ALIYUN = "aliyun";

    public static final String VOLCENGINE = "volcengine";

    public static final Set<String> ALL = Set.of(OPENAI, ALIYUN, VOLCENGINE);

    private LlmProviderTypeConstants() {
    }
}
