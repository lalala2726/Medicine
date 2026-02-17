package cn.zhangchuangla.medicine.agent.interceptor;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * 内部智能体请求头追踪拦截器。
 */
@Component
public class InternalAgentHeaderTraceInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(InternalAgentHeaderTraceInterceptor.class);
    private static final String HEADER_AGENT_KEY = "X-Agent-Key";
    private static final String HEADER_AGENT_TIMESTAMP = "X-Agent-Timestamp";
    private static final String HEADER_AGENT_NONCE = "X-Agent-Nonce";
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        InternalAgentHeaderTrace methodAnnotation =
                AnnotationUtils.findAnnotation(handlerMethod.getMethod(), InternalAgentHeaderTrace.class);
        InternalAgentHeaderTrace classAnnotation =
                AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), InternalAgentHeaderTrace.class);
        if (methodAnnotation == null && classAnnotation == null) {
            return true;
        }

        String agentKey = request.getHeader(HEADER_AGENT_KEY);
        String agentTimestamp = request.getHeader(HEADER_AGENT_TIMESTAMP);
        String agentNonce = request.getHeader(HEADER_AGENT_NONCE);
        String xForwardedFor = request.getHeader(HEADER_X_FORWARDED_FOR);

        List<String> missingHeaders = new ArrayList<>(3);
        if (StringUtils.isBlank(agentKey)) {
            missingHeaders.add(HEADER_AGENT_KEY);
        }
        if (StringUtils.isBlank(agentTimestamp)) {
            missingHeaders.add(HEADER_AGENT_TIMESTAMP);
        }
        if (StringUtils.isBlank(agentNonce)) {
            missingHeaders.add(HEADER_AGENT_NONCE);
        }

        log.info(
                "InternalAgentHeaderTrace method={} uri={} remoteAddr={} xForwardedFor={} xAgentKey={} xAgentTimestamp={} xAgentNonce={} missingHeaders={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                xForwardedFor,
                agentKey,
                agentTimestamp,
                agentNonce,
                missingHeaders
        );
        return true;
    }
}
