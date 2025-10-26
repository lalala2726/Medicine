package cn.zhangchuangla.medicine.common.security.aspect;

import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.exception.AccessDeniedException;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

/**
 * {@link IsAdmin} 注解切面，用于在方法执行前校验管理员角色。
 */
@Aspect
@Component
public class IsAdminAspect {

    private static final Logger log = LoggerFactory.getLogger(IsAdminAspect.class);

    @Around("@annotation(cn.zhangchuangla.medicine.common.security.annotation.IsAdmin) "
            + "|| @within(cn.zhangchuangla.medicine.common.security.annotation.IsAdmin)")
    public Object checkAdminPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 先从方法级别获取注解，不存在时再查找类级别注解
        IsAdmin isAdmin = resolveAnnotation(joinPoint);
        if (isAdmin != null) {
            // 当前用户没有满足角色要求时会直接抛出异常终止执行
            validateAdminRole(isAdmin);
        }
        return joinPoint.proceed();
    }

    private IsAdmin resolveAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = resolveMethod(joinPoint);
        IsAdmin annotation = AnnotationUtils.findAnnotation(method, IsAdmin.class);
        if (annotation != null) {
            return annotation;
        }
        Class<?> targetClass = joinPoint.getTarget() != null
                ? joinPoint.getTarget().getClass()
                : method.getDeclaringClass();
        return AnnotationUtils.findAnnotation(targetClass, IsAdmin.class);
    }

    private Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (joinPoint.getTarget() == null) {
            return method;
        }
        try {
            return joinPoint.getTarget()
                    .getClass()
                    .getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException ex) {
            // CGLIB 代理可能导致在目标类找不到同名方法，回退到声明方法
            return method;
        }
    }

    private void validateAdminRole(IsAdmin annotation) {
        String requiredRole = StringUtils.defaultIfBlank(annotation.value(), RolesConstant.ADMIN);
        String message = StringUtils.defaultIfBlank(annotation.message(),
                "需要管理员权限才能访问该资源");
        Set<String> roles = SecurityUtils.getRoles();

        boolean hasRequiredRole = roles.stream()
                .filter(Objects::nonNull)
                .anyMatch(role -> role.equalsIgnoreCase(requiredRole));
        if (!hasRequiredRole) {
            log.warn("Admin permission denied. requiredRole={}, userRoles={}", requiredRole, roles);
            throw new AccessDeniedException(message);
        }
    }
}
