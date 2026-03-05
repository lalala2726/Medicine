package cn.zhangchuangla.medicine.common.systemauth.annotation;

import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;

import java.lang.annotation.*;

/**
 * 系统级鉴权注解。
 * <p>
 * 标注在类或方法上时，接口将仅允许通过 X-Agent 系统签名认证访问。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Anonymous
public @interface AllowSystem {
}
