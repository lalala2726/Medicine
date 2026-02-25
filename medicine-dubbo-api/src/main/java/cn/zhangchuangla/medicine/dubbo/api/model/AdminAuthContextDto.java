package cn.zhangchuangla.medicine.dubbo.api.model;

import cn.zhangchuangla.medicine.model.entity.User;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 管理端智能体认证上下文。
 */
@Data
public class AdminAuthContextDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原始用户信息（包含认证所需字段）。
     */
    private User user;

    /**
     * 角色编码集合。
     */
    private Set<String> roles;

    /**
     * 权限编码集合。
     */
    private Set<String> permissions;
}
