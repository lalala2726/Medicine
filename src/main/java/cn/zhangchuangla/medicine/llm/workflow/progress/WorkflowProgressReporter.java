package cn.zhangchuangla.medicine.llm.workflow.progress;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;

/**
 * Reporter used by the workflow to push real-time progress updates back to the caller.
 * Implementations are responsible for converting the progress into SSE messages or any
 * other transport specific payload.
 */
public interface WorkflowProgressReporter {

    /**
     * Publish a workflow stage update.
     *
     * @param stage stage code to publish
     * @param message human readable description
     * @param finished whether this stage finishes the workflow stream
     */
    void publishStage(ChatStageEnum stage, String message, boolean finished);

    default void publishStage(ChatStageEnum stage, String message) {
        publishStage(stage, message, false);
    }

    /**
     * Notify a tool invocation start.
     *
     * @param toolName Spring AI tool name
     * @param message contextual information for UI display
     */
    void publishToolInvoke(String toolName, String message);

    /**
     * Notify a tool invocation result.
     *
     * @param toolName Spring AI tool name
     * @param result short text result for display
     */
    void publishToolResult(String toolName, String result);

    /**
     * Emit a heartbeat message so SSE clients keep the TCP connection alive.
     */
    default void publishHeartbeat() {
        publishStage(ChatStageEnum.HEARTBEAT, null);
    }

    /**
     * Send a piece of response content in streaming mode.
     *
     * @param content stream fragment
     */
    void publishResponseChunk(String content);

    /**
     * Complete the assistant response with the persisted message uuid.
     *
     * @param messageUuid identifier generated after storing assistant message
     */
    default void publishResponseCompleted(String messageUuid) {
        publishResponseCompleted(messageUuid, ChatStageEnum.COMPLETED);
    }

    void publishResponseCompleted(String messageUuid, ChatStageEnum finalStage);

    /**
     * @return {@code true} if downstream consumer has cancelled the stream
     */
    boolean isCancelled();
}
