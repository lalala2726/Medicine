package cn.zhangchuangla.medicine.model.enums;

import lombok.Getter;

/**
 * 知识库文档切片本地阶段枚举。
 */
@Getter
public enum KbDocumentChunkStageEnum {

    /**
     * 本地已创建占位切片，等待 AI 处理。
     */
    PENDING("PENDING", "待处理", "本地已创建占位切片，等待 AI 处理"),

    /**
     * AI 已开始处理切片任务。
     */
    STARTED("STARTED", "已开始", "AI 已开始处理切片任务"),

    /**
     * 切片任务已完成。
     */
    COMPLETED("COMPLETED", "已完成", "切片任务已完成"),

    /**
     * 切片任务处理失败。
     */
    FAILED("FAILED", "失败", "切片任务处理失败");

    /**
     * 阶段编码。
     */
    private final String code;

    /**
     * 中文名称。
     */
    private final String name;

    /**
     * 阶段描述。
     */
    private final String description;

    KbDocumentChunkStageEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据阶段编码查找枚举。
     *
     * @param code 阶段编码
     * @return 对应枚举；不存在时返回 null
     */
    public static KbDocumentChunkStageEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalizedCode = code.trim();
        for (KbDocumentChunkStageEnum stage : values()) {
            if (stage.code.equalsIgnoreCase(normalizedCode)) {
                return stage;
            }
        }
        return null;
    }

    /**
     * 判断当前枚举是否匹配指定阶段编码。
     *
     * @param code 阶段编码
     * @return true 表示匹配
     */
    public boolean matches(String code) {
        return code != null && this.code.equalsIgnoreCase(code.trim());
    }
}
