package cn.zhangchuangla.medicine.llm.workflow.progress;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;

import java.util.Optional;

/**
 * 线程本地持有器，允许工作流节点和工具访问当前的进度报告器，
 * 而无需耦合到特定传输的详细信息。Graph 引擎在调用者线程上顺序执行节点，
 * 因此 {@link ThreadLocal} 提供了轻量级的传递机制。
 */
public final class WorkflowProgressContextHolder {

    private static final ThreadLocal<WorkflowProgressReporter> CONTEXT = new ThreadLocal<>();

    private WorkflowProgressContextHolder() {
    }

    public static void setReporter(WorkflowProgressReporter reporter) {
        if (reporter == null) {
            throw new IllegalArgumentException("reporter cannot be null");
        }
        CONTEXT.set(reporter);
    }

    public static WorkflowProgressReporter getReporter() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Optional<WorkflowProgressReporter> current() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void publishStage(ChatStageEnum stage, String message) {
        current().ifPresent(reporter -> reporter.publishStage(stage, message));
    }

    public static void publishToolInvoke(String toolName, String message) {
        current().ifPresent(reporter -> reporter.publishToolInvoke(toolName, message));
    }

    public static void publishToolResult(String toolName, String result) {
        current().ifPresent(reporter -> reporter.publishToolResult(toolName, result));
    }

    public static void publishResponseChunk(String content) {
        current().ifPresent(reporter -> reporter.publishResponseChunk(content));
    }

    /**
     * 将心跳事件传播到当前报告器（如果存在）。
     * 让 SSE 客户端了解后端仍在工作。
     */
    public static void publishHeartbeat() {
        current().ifPresent(WorkflowProgressReporter::publishHeartbeat);
    }

    public static void publishResponseCompleted(String messageUuid) {
        current().ifPresent(reporter -> reporter.publishResponseCompleted(messageUuid));
    }

    public static void publishResponseCompleted(String messageUuid, ChatStageEnum finalStage) {
        current().ifPresent(reporter -> reporter.publishResponseCompleted(messageUuid, finalStage));
    }

    public static boolean isCancelled() {
        return current().map(WorkflowProgressReporter::isCancelled).orElse(false);
    }

    public static void ifPresent(java.util.function.Consumer<WorkflowProgressReporter> consumer) {
        WorkflowProgressReporter reporter = CONTEXT.get();
        if (reporter != null) {
            consumer.accept(reporter);
        }
    }
}
