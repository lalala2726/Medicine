package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.PermissionAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.PermissionUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.PermissionTreeVo;
import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.model.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

public interface PermissionService extends IService<Permission> {

    /**
     * 获取权限树
     */
    List<PermissionTreeVo> permissionTree();

    /**
     * 根据ID获取权限详情
     */
    Permission getPermissionById(Long id);

    /**
     * 添加权限
     */
    boolean addPermission(PermissionAddRequest request);

    /**
     * 修改权限
     */
    boolean updatePermissionById(PermissionUpdateRequest request);

    /**
     * 批量删除权限
     */
    boolean deletePermissionByIds(List<Long> ids);

    /**
     * 获取权限选项列表
     */
    List<Option<Long>> permissionOption();

    /**
     * 根据用户ID获取权限列表
     */
    Set<String> getPermissionCodesByUserId(Long userId);
}
