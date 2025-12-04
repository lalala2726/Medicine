package cn.zhangchuangla.medicine.common.rabbitmq.constants;

/**
 * 知识库导入相关的队列/交换机常量。
 */
public interface KnowledgeBaseQueueConstants {

    String EXCHANGE = "kb.import.exchange";
    String QUEUE = "kb.import.queue";
    String ROUTING_IMPORT = "kb.import";
}
