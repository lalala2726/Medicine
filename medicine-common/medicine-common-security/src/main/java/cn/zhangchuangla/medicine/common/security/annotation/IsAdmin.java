package cn.zhangchuangla.medicine.common.security.annotation;

import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;

import java.lang.annotation.*;

/**
 * 标记方法或类仅允许指定管理员角色访问的注解。
 * <p>
 * 默认情况下要求当前用户具备 {@code admin} 角色，可以通过 {@link #value()} 覆盖默认角色。
 * 当权限不足时，框架会抛出 {@code AdminAccessException}，交由全局异常处理器转换为 HTTP 403。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IsAdmin {

    /**
     * 需要满足的管理员角色标识，默认为 {@link RolesConstant#ADMIN}。
     */
    String value() default RolesConstant.ADMIN;

    /**
     * 当权限不足时抛出的异常消息。
     */
    String message() default "需要管理员权限才能访问该资源";
}
