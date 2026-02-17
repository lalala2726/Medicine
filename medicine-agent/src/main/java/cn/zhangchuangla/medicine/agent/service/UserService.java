package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;

import java.util.Set;

/**
 * 智能体用户服务接口。
 * <p>
 * 提供用户相关的查询服务，包括用户信息、角色和权限的查询。
 *
 * @author Chuang
 */
public interface UserService {

    /**
     * 获取当前用户的详细信息。
     *
     * @param userId 用户 ID
     * @return 用户详细信息
     */
    UserVo getCurrentUser(Long userId);

    /**
     * 获取用户的认证信息。
     *
     * @param userId 用户 ID
     * @return 用户认证信息
     */
    AuthUserDto getUser(Long userId);

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);

    /**
     * 获取用户的角色编码集合。
     *
     * @param userId 用户 ID
     * @return 角色编码集合
     */
    Set<String> getUserRolesByUserId(Long userId);

    /**
     * 获取用户的权限编码集合。
     *
     * @param userId 用户 ID
     * @return 权限编码集合
     */
    Set<String> getUserPermissionCodesByUserId(Long userId);
}
