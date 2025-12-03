package cn.zhangchuangla.medicine.llm.aspect;

import cn.zhangchuangla.medicine.llm.annotation.ToolCallStage;
import cn.zhangchuangla.medicine.llm.model.enums.EventType;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 拦截标记了 {@link ToolCallStage} 的工具方法，在调用前后发送 SSE 进度事件。
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ToolCallStageAspect {

    private final SseMessageInjector messageInjector;

    @Around("@annotation(toolCallStage)")
    public Object injectToolStage(ProceedingJoinPoint joinPoint, ToolCallStage toolCallStage) throws Throwable {
        String startDescription = resolveDescription(toolCallStage.start(), EventType.TOOL_CALL_START, joinPoint);
        String endDescription = resolveDescription(toolCallStage.end(), EventType.TOOL_CALL_END, joinPoint);

        sendSafely(EventType.TOOL_CALL_START, startDescription);
        try {
            return joinPoint.proceed();
        } finally {
            sendSafely(EventType.TOOL_CALL_END, endDescription);
        }
    }

    private void sendSafely(EventType eventType, String description) {
        try {
            messageInjector.callToolAction(eventType, description);
        } catch (Exception ex) {
            log.warn("工具事件发送失败，eventType={}, description={}", eventType, description, ex);
        }
    }

    private String resolveDescription(String configured, EventType eventType, ProceedingJoinPoint joinPoint) {
        if (StringUtils.hasText(configured)) {
            return configured;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Tool tool = AnnotationUtils.findAnnotation(signature.getMethod(), Tool.class);
        String base = (tool != null && StringUtils.hasText(tool.description()))
                ? tool.description()
                : signature.getMethod().getName();

        return eventType == EventType.TOOL_CALL_START
                ? "开始调用工具：" + base
                : "工具调用完成：" + base;
    }
}
