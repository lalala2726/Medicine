package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

public interface UserRoleService extends IService<UserRole> {

    /**
     * 获取用户角色ID
     */
    Set<Long> getUserRoleByUserId(Long id);

    /**
     * 更新用户角色
     */
    void updateUserRole(Long id, Set<Long> roles);
}
