package cn.zhangchuangla.medicine.common.rabbitmq.constants;

/**
 * 知识库导入相关的队列/交换机常量。
 */
public interface KnowledgeBaseQueueConstants {

    String EXCHANGE = "kb.import.exchange";
    String QUEUE = "kb.import.queue";
    String ROUTING_IMPORT = "kb.import";

    String VECTOR_DELETE_QUEUE = "kb.vector.delete.queue";
    String ROUTING_VECTOR_DELETE = "kb.vector.delete";

    String KB_DELETE_QUEUE = "kb.delete.queue";
    String ROUTING_KB_DELETE = "kb.delete";

    String CHUNK_UPDATE_QUEUE = "kb.chunk.update.queue";
    String ROUTING_CHUNK_UPDATE = "kb.chunk.update";
}
