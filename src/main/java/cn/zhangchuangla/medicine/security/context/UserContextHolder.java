package cn.zhangchuangla.medicine.security.context;

import cn.zhangchuangla.medicine.security.entity.SysUserDetails;

/**
 * Stores the current authenticated {@link SysUserDetails} in a ThreadLocal so that
 * downstream components (e.g. LLM tools) can retrieve the user even when running
 * outside of the original request thread.
 */
public final class UserContextHolder {

    private static final ThreadLocal<SysUserDetails> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(SysUserDetails userDetails) {
        if (userDetails == null) {
            clear();
            return;
        }
        CONTEXT.set(userDetails);
    }

    public static SysUserDetails get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
