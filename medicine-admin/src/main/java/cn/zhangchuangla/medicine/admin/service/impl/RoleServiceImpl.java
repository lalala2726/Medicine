package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.RoleMapper;
import cn.zhangchuangla.medicine.admin.model.request.RoleAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.RoleListRequest;
import cn.zhangchuangla.medicine.admin.model.request.RolePermissionUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.request.RoleUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.RolePermissionService;
import cn.zhangchuangla.medicine.admin.service.RoleService;
import cn.zhangchuangla.medicine.admin.service.UserRoleService;
import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.Role;
import cn.zhangchuangla.medicine.model.entity.RolePermission;
import cn.zhangchuangla.medicine.model.entity.UserRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
        implements RoleService, BaseService {

    private final RoleMapper roleMapper;
    private final UserRoleService userRoleService;
    private final RolePermissionService rolePermissionService;

    @Override
    public Page<Role> roleList(RoleListRequest query) {
        Page<Role> page = query.toPage();
        return roleMapper.roleList(page, query);
    }

    @Override
    public Role getRoleById(Long id) {
        Assert.isPositive(id, "角色ID必须大于0");
        return getById(id);
    }

    @Override
    public boolean addRole(RoleAddRequest request) {
        Assert.notNull(request, "角色信息不能为空");
        checkRoleCodeUnique(request.getRoleCode(), null);
        checkRoleNameUnique(request.getRoleName(), null);
        Role role = copyProperties(request, Role.class);
        return save(role);
    }

    @Override
    public boolean updateRoleById(RoleUpdateRequest request) {
        Assert.notNull(request, "角色信息不能为空");
        Assert.isPositive(request.getId(), "角色ID必须大于0");
        checkRoleCodeUnique(request.getRoleCode(), request.getId());
        checkRoleNameUnique(request.getRoleName(), request.getId());
        Role role = copyProperties(request, Role.class);
        return updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "角色ID不能为空");
        }
        if (ids.contains(RolesConstant.SUPER_ADMIN_ROLE_ID)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "超级管理员角色禁止删除");
        }

        boolean hasAssignedUsers = userRoleService.lambdaQuery()
                .in(UserRole::getRoleId, ids)
                .count() > 0;
        Assert.isTrue(!hasAssignedUsers, "已分配给用户的角色禁止删除");

        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, ids));
        return removeByIds(ids);
    }

    @Override
    public void isRoleExistById(Long id) {
        Assert.isPositive(id, "角色ID必须大于0");
        Role role = getById(id);
        Assert.isTrue(role != null, "角色不存在");
    }

    @Override
    public void isRoleExistById(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        long count = lambdaQuery().in(Role::getId, ids).count();
        Assert.isTrue(count == ids.size(), "角色不存在");
    }

    @Override
    public boolean isRoleExitsByRoleCode(String roleCode) {
        Assert.notEmpty(roleCode, "角色编码不能为空");
        return lambdaQuery().eq(Role::getRoleCode, roleCode).count() > 0;
    }

    @Override
    public List<Long> getRolePermission(Long id) {
        return rolePermissionService.getRolePermission(id);
    }

    @Override
    public boolean updateRolePermission(RolePermissionUpdateRequest request) {
        return rolePermissionService.updateRolePermission(request);
    }

    @Override
    public List<Option<Long>> roleOption() {
        return lambdaQuery()
                .eq(Role::getStatus, 0)
                .list()
                .stream()
                .map(role -> new Option<>(role.getId(), role.getRoleName()))
                .toList();
    }

    @Override
    public Set<String> getUserRoleByUserId(Long userId) {
        Assert.isPositive(userId, "用户ID必须大于0");
        Set<Long> roleIds = userRoleService.getUserRoleByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Set.of();
        }
        List<String> roleCodes = lambdaQuery()
                .in(Role::getId, roleIds)
                .eq(Role::getStatus, 0)
                .list()
                .stream()
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
        if (CollectionUtils.isEmpty(roleCodes)) {
            return Set.of();
        }
        return Set.copyOf(roleCodes);
    }

    @Override
    public Set<Long> getRoleIdByUserId(Long userId) {
        Assert.isPositive(userId, "用户ID必须大于0");
        Set<Long> roleIds = userRoleService.getUserRoleByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Set.of();
        }
        List<Long> existingIds = lambdaQuery()
                .in(Role::getId, roleIds)
                .eq(Role::getStatus, 0)
                .list()
                .stream()
                .map(Role::getId)
                .toList();
        if (CollectionUtils.isEmpty(existingIds)) {
            return Set.of();
        }
        return Set.copyOf(existingIds);
    }

    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        if (roleCode == null) {
            return;
        }
        boolean exists = lambdaQuery()
                .eq(Role::getRoleCode, roleCode)
                .ne(excludeId != null, Role::getId, excludeId)
                .count() > 0;
        Assert.isTrue(!exists, "角色标识已存在");
    }

    private void checkRoleNameUnique(String roleName, Long excludeId) {
        if (roleName == null) {
            return;
        }
        boolean exists = lambdaQuery()
                .eq(Role::getRoleName, roleName)
                .ne(excludeId != null, Role::getId, excludeId)
                .count() > 0;
        Assert.isTrue(!exists, "角色名称已存在");
    }
}
