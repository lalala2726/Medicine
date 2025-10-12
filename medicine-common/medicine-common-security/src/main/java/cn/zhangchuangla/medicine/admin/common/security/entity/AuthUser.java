package cn.zhangchuangla.medicine.admin.common.security.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 通用认证用户模型，屏蔽不同端的用户实体差异。
 */
@Data
@Builder
public class AuthUser implements Serializable {

    @Serial
    private static final long serialVersionUID = -904867432593234359L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 登录名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 账号状态，0-正常，非0表示不可用
     */
    private Integer status;

    /**
     * 角色编码集合
     */
    @Builder.Default
    private Set<String> roles = Collections.emptySet();

    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 账号是否未过期
     */
    @Builder.Default
    private boolean accountNonExpired = true;

    /**
     * 账号是否未锁定
     */
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * 凭证是否未过期
     */
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * 最近登录时间等自定义属性
     */
    @Builder.Default
    private Map<String, Object> attributes = Collections.emptyMap();

    /**
     * 创建时间（可选）
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间（可选）
     */
    private LocalDateTime updatedAt;
}
