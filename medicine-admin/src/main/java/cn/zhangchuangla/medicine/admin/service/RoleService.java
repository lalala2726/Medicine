package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.RoleAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.RoleListRequest;
import cn.zhangchuangla.medicine.admin.model.request.RolePermissionUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.request.RoleUpdateRequest;
import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.model.entity.Role;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表
     */
    Page<Role> roleList(RoleListRequest query);

    /**
     * 根据ID获取角色详情
     */
    Role getRoleById(Long id);

    /**
     * 添加角色
     */
    boolean addRole(RoleAddRequest request);

    /**
     * 修改角色信息
     */
    boolean updateRoleById(RoleUpdateRequest request);

    /**
     * 批量删除角色
     */
    boolean deleteRoleByIds(List<Long> ids);

    /**
     * 判断角色是否存在
     */
    void isRoleExistById(Long id);

    /**
     * 判断角色是否存在
     */
    void isRoleExistById(Set<Long> ids);

    /**
     * 根据角色编码判断角色是否存在
     */
    boolean isRoleExitsByRoleCode(String roleCode);

    /**
     * 获取角色权限
     */
    List<Long> getRolePermission(Long id);

    /**
     * 更新角色权限
     */
    boolean updateRolePermission(RolePermissionUpdateRequest request);

    /**
     * 获取角色选项
     */
    List<Option<Long>> roleOption();

    /**
     * 根据用户ID获取用户角色
     */
    Set<String> getUserRoleByUserId(Long userId);

    /**
     * 根据用户ID获取角色ID
     */
    Set<Long> getRoleIdByUserId(Long userId);
}
