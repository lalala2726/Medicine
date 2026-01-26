package cn.zhangchuangla.medicine.llm.milvus.enums;

/**
 * 文档切分策略类型
 * 用于统一管理所有可用的文档分片方式。
 */
public enum DocumentChunkStrategy {

    /**
     * 自动智能切分（结合句子级自然切分 + token/长度控制）
     * 系统默认推荐模式。
     */
    SMART,

    /**
     * 按固定长度切分（如每 300 字 / 每 500 token）
     */
    FIXED_LENGTH,

    /**
     * 按页面切分（适用于 PDF）
     */
    BY_PAGE,

    /**
     * 按标题层级切分（H1/H2/H3、段落标题等）
     * 适用于 Word、Markdown、说明书类文档
     */
    BY_HEADING,

    /**
     * 自定义切分（用户可在配置中指定标识符、正则等）
     */
    CUSTOM
}
