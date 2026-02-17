package cn.zhangchuangla.medicine.agent.annotation;

import java.lang.annotation.*;

/**
 * 内部智能体请求头追踪注解。
 * <p>
 * 标注在类或方法上时，会记录 X-Agent-Key、X-Agent-Timestamp、X-Agent-Nonce 等请求头信息。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalAgentHeaderTrace {
}
