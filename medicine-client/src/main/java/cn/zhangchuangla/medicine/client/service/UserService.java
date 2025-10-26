package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

/**
 * @author Chuang
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据用户ID查询用户角色集合
     *
     * @param userId 用户ID
     * @return 用户角色集合
     */
    Set<String> getUserRolesByUserId(Long userId);

    /**
     * 根据用户名查询用户角色集合
     *
     * @param username 用户名
     * @return 用户角色集合
     */
    Set<String> getUserRolesByUserName(String username);
}
