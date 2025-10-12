package cn.zhangchuangla.medicine.security;

import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限认证组件
 * <p>
 * 提供权限校验的方法，如单个权限校验、多权限校验、用户身份校验等。
 * </p>
 *
 * @author Chuang
 */
@Slf4j
@Service("ss")
public class PermissionAuth {

    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return SecurityUtils.getRoles().contains(RolesConstant.SUPER_ADMIN);
    }

}
