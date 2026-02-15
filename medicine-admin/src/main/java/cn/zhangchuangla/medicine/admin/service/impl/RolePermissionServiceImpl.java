package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.PermissionMapper;
import cn.zhangchuangla.medicine.admin.mapper.RolePermissionMapper;
import cn.zhangchuangla.medicine.admin.model.request.RolePermissionUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.RolePermissionService;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.Permission;
import cn.zhangchuangla.medicine.model.entity.RolePermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission>
        implements RolePermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<Long> getRolePermission(Long id) {
        return getRolePermission(List.of(id));
    }

    @Override
    public List<Long> getRolePermission(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        List<RolePermission> list = lambdaQuery().in(RolePermission::getRoleId, ids).list();
        return list.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .toList();
    }

    @Override
    public List<Long> getAllPermissionIds() {
        return permissionMapper.selectList(new LambdaQueryWrapper<>())
                .stream()
                .map(Permission::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRolePermission(RolePermissionUpdateRequest request) {
        Assert.notNull(request, "角色权限信息不能为空");
        Assert.isPositive(request.getRoleId(), "角色ID必须大于0");
        if (RolesConstant.SUPER_ADMIN_ROLE_ID.equals(request.getRoleId())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "超级管理员角色禁止修改");
        }

        remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, request.getRoleId()));
        if (CollectionUtils.isEmpty(request.getPermissionIds())) {
            return true;
        }

        Set<Long> existingPermissionIds = permissionMapper
                .selectList(new LambdaQueryWrapper<Permission>().in(Permission::getId, request.getPermissionIds()))
                .stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        if (existingPermissionIds.isEmpty()) {
            return true;
        }

        List<RolePermission> rolePermissions = existingPermissionIds.stream()
                .map(permissionId -> {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(request.getRoleId());
                    rolePermission.setPermissionId(permissionId);
                    return rolePermission;
                })
                .toList();
        return saveBatch(rolePermissions);
    }
}
