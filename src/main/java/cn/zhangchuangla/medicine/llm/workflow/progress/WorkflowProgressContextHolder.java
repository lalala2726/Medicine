package cn.zhangchuangla.medicine.llm.workflow.progress;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;

import java.util.Optional;

/**
 * Thread local holder that allows workflow nodes and tools to access the current
 * progress reporter without coupling to transport specific details. The Graph engine
 * executes nodes sequentially on the caller thread, therefore a {@link ThreadLocal}
 * provides a lightweight hand-off mechanism.
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
     * Propagate a heartbeat event to the current reporter (if any).
     * Keeps SSE clients aware that the backend is still working.
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
