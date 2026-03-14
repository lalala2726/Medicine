package cn.zhangchuangla.medicine.model.constants;

import java.util.Set;

/**
 * 大模型类型常量。
 *
 * @author Chuang
 */
public final class LlmModelTypeConstants {

    public static final String CHAT = "CHAT";

    public static final String EMBEDDING = "EMBEDDING";

    public static final Set<String> ALL = Set.of(CHAT, EMBEDDING);

    private LlmModelTypeConstants() {
    }
}
