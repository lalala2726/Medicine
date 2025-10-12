package cn.zhangchuangla.medicine.ai.workflow.progress;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;

/**
 * 工作流使用的报告器，用于将实时进度更新推送回调用者。
 * 实现负责将进度转换为 SSE 消息或任何其他特定传输的负载数据。
 */
public interface WorkflowProgressReporter {

    /**
     * 发布工作流阶段更新。
     *
     * @param stage    要发布的阶段代码
     * @param message  人类可读的描述
     * @param finished 此阶段是否完成工作流流
     */
    void publishStage(ChatStageEnum stage, String message, boolean finished);

    default void publishStage(ChatStageEnum stage, String message) {
        publishStage(stage, message, false);
    }

    /**
     * 通知工具调用开始。
     *
     * @param toolName Spring AI 工具名称
     * @param message  用于 UI 显示的上下文信息
     */
    void publishToolInvoke(String toolName, String message);

    /**
     * 通知工具调用结果。
     *
     * @param toolName Spring AI 工具名称
     * @param result   用于显示的简短文本结果
     */
    void publishToolResult(String toolName, String result);

    /**
     * 发送心跳消息，使 SSE 客户端保持 TCP 连接活跃。
     */
    default void publishHeartbeat() {
        publishStage(ChatStageEnum.HEARTBEAT, null);
    }

    /**
     * 在流式模式下发送一段响应内容。
     *
     * @param content 流片段
     */
    void publishResponseChunk(String content);

    /**
     * 使用持久化的消息 uuid 完成助手响应。
     *
     * @param messageUuid 存储助手消息后生成的标识符
     */
    default void publishResponseCompleted(String messageUuid) {
        publishResponseCompleted(messageUuid, ChatStageEnum.COMPLETED);
    }

    void publishResponseCompleted(String messageUuid, ChatStageEnum finalStage);

    /**
     * @return 如果下游消费者已取消流，则返回 {@code true}
     */
    boolean isCancelled();
}
