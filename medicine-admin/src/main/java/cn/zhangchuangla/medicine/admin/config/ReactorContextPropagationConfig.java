package cn.zhangchuangla.medicine.admin.config;

import cn.zhangchuangla.medicine.ai.workflow.context.UserContextHolder;
import cn.zhangchuangla.medicine.ai.workflow.progress.WorkflowProgressContextHolder;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Hooks;

/**
 * Enables Reactor context propagation so that security and workflow related ThreadLocals
 * remain available on worker threads used by reactive pipelines and LLM tool callbacks.
 */
@Slf4j
@Configuration
public class ReactorContextPropagationConfig {

    @PostConstruct
    public void setupContextPropagation() {
        Hooks.enableAutomaticContextPropagation();
        ContextRegistry registry = ContextRegistry.getInstance();

        registry.registerThreadLocalAccessor("security-context",
                () -> {
                    SecurityContext context = SecurityContextHolder.getContext();
                    return context != null && context.getAuthentication() != null ? context : null;
                },
                context -> {
                    if (context == null) {
                        SecurityContextHolder.clearContext();
                    } else {
                        SecurityContextHolder.setContext(context);
                    }
                },
                SecurityContextHolder::clearContext);

        registry.registerThreadLocalAccessor("workflow-progress-reporter",
                WorkflowProgressContextHolder::getReporter,
                reporter -> {
                    if (reporter == null) {
                        WorkflowProgressContextHolder.clear();
                    } else {
                        WorkflowProgressContextHolder.setReporter(reporter);
                    }
                },
                WorkflowProgressContextHolder::clear);

        registry.registerThreadLocalAccessor("user-tools-context",
                UserContextHolder::get,
                user -> {
                    if (user == null) {
                        UserContextHolder.clear();
                    } else {
                        UserContextHolder.set(user);
                    }
                },
                UserContextHolder::clear);

        log.info("Reactor automatic context propagation enabled for security, workflow and user contexts");
    }
}
