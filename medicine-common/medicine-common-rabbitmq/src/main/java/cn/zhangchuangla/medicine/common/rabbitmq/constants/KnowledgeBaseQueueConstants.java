package cn.zhangchuangla.medicine.common.rabbitmq.constants;

/**
 * 知识库导入相关的队列/交换机常量。
 */
public final class KnowledgeBaseQueueConstants {

    /**
     * 知识库导入交换机。
     */
    public static final String EXCHANGE = "kb.import.exchange";
    /**
     * 知识库导入队列。
     */
    public static final String QUEUE = "kb.import.queue";
    /**
     * 知识库导入路由键。
     */
    public static final String ROUTING_IMPORT = "kb.import";

    /**
     * 知识库向量删除队列。
     */
    public static final String VECTOR_DELETE_QUEUE = "kb.vector.delete.queue";
    /**
     * 知识库向量删除路由键。
     */
    public static final String ROUTING_VECTOR_DELETE = "kb.vector.delete";

    /**
     * 知识库删除队列。
     */
    public static final String KB_DELETE_QUEUE = "kb.delete.queue";
    /**
     * 知识库删除路由键。
     */
    public static final String ROUTING_KB_DELETE = "kb.delete";

    /**
     * 知识库切片更新队列。
     */
    public static final String CHUNK_UPDATE_QUEUE = "kb.chunk.update.queue";
    /**
     * 知识库切片更新路由键。
     */
    public static final String ROUTING_CHUNK_UPDATE = "kb.chunk.update";

    private KnowledgeBaseQueueConstants() {
    }
}
