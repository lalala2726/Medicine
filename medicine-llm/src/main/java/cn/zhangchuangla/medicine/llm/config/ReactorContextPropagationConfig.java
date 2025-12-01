package cn.zhangchuangla.medicine.llm.config;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Hooks;

/**
 * 配置 Reactor 的上下文传播，解决 SSE/异步场景下 SecurityContext 丢失的问题。
 *
 * @author Chuang
 */
@Configuration
public class ReactorContextPropagationConfig {

    @PostConstruct
    public void setup() {
        // 1) 打开 Reactor 自动上下文传播
        Hooks.enableAutomaticContextPropagation();

        // 2) 注册 SecurityContext 的 ThreadLocal 访问器
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                "security-context",
                () -> {
                    SecurityContext ctx = SecurityContextHolder.getContext();
                    return (ctx != null && ctx.getAuthentication() != null) ? ctx : null;
                },
                ctx -> {
                    if (ctx == null) {
                        SecurityContextHolder.clearContext();
                    } else {
                        SecurityContextHolder.setContext(ctx);
                    }
                },
                SecurityContextHolder::clearContext
        );
    }
}
