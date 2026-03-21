package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallProductTagMapper;
import cn.zhangchuangla.medicine.admin.mapper.MallProductTagTypeMapper;
import cn.zhangchuangla.medicine.admin.service.MallProductTagTypeService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.constants.MallProductTagConstants;
import cn.zhangchuangla.medicine.model.entity.MallProductTag;
import cn.zhangchuangla.medicine.model.entity.MallProductTagType;
import cn.zhangchuangla.medicine.model.request.MallProductTagTypeAddRequest;
import cn.zhangchuangla.medicine.model.request.MallProductTagTypeListQueryRequest;
import cn.zhangchuangla.medicine.model.request.MallProductTagTypeStatusUpdateRequest;
import cn.zhangchuangla.medicine.model.request.MallProductTagTypeUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.MallProductTagTypeAdminVo;
import cn.zhangchuangla.medicine.model.vo.MallProductTagTypeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 商品标签类型服务实现。
 *
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallProductTagTypeServiceImpl extends ServiceImpl<MallProductTagTypeMapper, MallProductTagType>
        implements MallProductTagTypeService, BaseService {

    /**
     * 标签类型编码格式校验器。
     */
    private static final Pattern TYPE_CODE_PATTERN = Pattern.compile(MallProductTagConstants.TYPE_CODE_PATTERN);

    /**
     * 商品标签 Mapper。
     */
    private final MallProductTagMapper mallProductTagMapper;

    /**
     * 分页查询标签类型列表。
     *
     * @param request 查询参数
     * @return 标签类型分页结果
     */
    @Override
    public Page<MallProductTagTypeAdminVo> listTypes(MallProductTagTypeListQueryRequest request) {
        Assert.notNull(request, "查询参数不能为空");
        LambdaQueryWrapper<MallProductTagType> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getCode())) {
            queryWrapper.like(MallProductTagType::getCode, request.getCode().trim().toUpperCase());
        }
        if (StringUtils.hasText(request.getName())) {
            queryWrapper.like(MallProductTagType::getName, request.getName().trim());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(MallProductTagType::getStatus, request.getStatus());
        }
        queryWrapper.orderByAsc(MallProductTagType::getSort)
                .orderByDesc(MallProductTagType::getId);
        Page<MallProductTagType> typePage = page(request.toPage(), queryWrapper);
        List<MallProductTagTypeAdminVo> rows = typePage.getRecords().stream()
                .map(this::toAdminVo)
                .toList();
        Page<MallProductTagTypeAdminVo> result = new Page<>(typePage.getCurrent(), typePage.getSize(), typePage.getTotal());
        result.setRecords(rows);
        return result;
    }

    /**
     * 查询标签类型详情。
     *
     * @param id 标签类型ID
     * @return 标签类型详情
     */
    @Override
    public MallProductTagTypeAdminVo getTypeById(Long id) {
        return toAdminVo(getTypeEntityById(id));
    }

    /**
     * 查询标签类型实体。
     *
     * @param id 标签类型ID
     * @return 标签类型实体
     */
    @Override
    public MallProductTagType getTypeEntityById(Long id) {
        Assert.isPositive(id, "标签类型ID不能为空");
        MallProductTagType type = getById(id);
        if (type == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "标签类型不存在");
        }
        return type;
    }

    /**
     * 根据编码查询标签类型实体。
     *
     * @param code 标签类型编码
     * @return 标签类型实体
     */
    @Override
    public MallProductTagType getTypeEntityByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return lambdaQuery()
                .eq(MallProductTagType::getCode, normalizeCode(code))
                .one();
    }

    /**
     * 按ID列表查询标签类型映射。
     *
     * @param typeIds 标签类型ID列表
     * @return 标签类型映射
     */
    @Override
    public Map<Long, MallProductTagType> listTypeEntityMapByIds(List<Long> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) {
            return Map.of();
        }
        return listByIds(typeIds).stream()
                .collect(Collectors.toMap(MallProductTagType::getId, type -> type));
    }

    /**
     * 查询启用标签类型下拉列表。
     *
     * @return 启用标签类型列表
     */
    @Override
    public List<MallProductTagTypeVo> option() {
        return lambdaQuery()
                .eq(MallProductTagType::getStatus, MallProductTagConstants.STATUS_ENABLED)
                .orderByAsc(MallProductTagType::getSort)
                .orderByDesc(MallProductTagType::getId)
                .list()
                .stream()
                .map(this::toVo)
                .toList();
    }

    /**
     * 新增标签类型。
     *
     * @param request 新增请求
     * @return 是否成功
     */
    @Override
    public boolean addType(MallProductTagTypeAddRequest request) {
        Assert.notNull(request, "标签类型信息不能为空");
        MallProductTagType type = new MallProductTagType();
        type.setCode(normalizeCode(request.getCode()));
        type.setName(normalizeName(request.getName()));
        type.setSort(normalizeSort(request.getSort()));
        type.setStatus(normalizeStatus(request.getStatus()));
        type.setCreateTime(new Date());
        type.setCreateBy(getUsername());
        try {
            return save(type);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "标签类型编码或名称已存在");
        }
    }

    /**
     * 修改标签类型。
     *
     * @param request 修改请求
     * @return 是否成功
     */
    @Override
    public boolean updateType(MallProductTagTypeUpdateRequest request) {
        Assert.notNull(request, "标签类型信息不能为空");
        MallProductTagType existingType = getTypeEntityById(request.getId());
        existingType.setName(normalizeName(request.getName()));
        existingType.setSort(normalizeSort(request.getSort()));
        existingType.setUpdateTime(new Date());
        existingType.setUpdateBy(getUsername());
        try {
            return updateById(existingType);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "标签类型名称已存在");
        }
    }

    /**
     * 修改标签类型状态。
     *
     * @param request 状态请求
     * @return 是否成功
     */
    @Override
    public boolean updateTypeStatus(MallProductTagTypeStatusUpdateRequest request) {
        Assert.notNull(request, "标签类型状态信息不能为空");
        MallProductTagType existingType = getTypeEntityById(request.getId());
        Integer targetStatus = normalizeStatus(request.getStatus());
        if (Objects.equals(existingType.getStatus(), targetStatus)) {
            return true;
        }
        existingType.setStatus(targetStatus);
        existingType.setUpdateTime(new Date());
        existingType.setUpdateBy(getUsername());
        return updateById(existingType);
    }

    /**
     * 删除标签类型。
     *
     * @param id 标签类型ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteType(Long id) {
        MallProductTagType type = getTypeEntityById(id);
        Long count = mallProductTagMapper.selectCount(new LambdaQueryWrapper<MallProductTag>()
                .eq(MallProductTag::getTypeId, id));
        if (count != null && count > 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "标签类型下存在标签，请先禁用标签类型");
        }
        return baseMapper.physicalDeleteById(type.getId()) > 0;
    }

    /**
     * 规范化标签类型编码。
     *
     * @param code 原始编码
     * @return 规范化后的编码
     */
    private String normalizeCode(String code) {
        Assert.notEmpty(code, "标签类型编码不能为空");
        String normalizedCode = code.trim().toUpperCase();
        if (!TYPE_CODE_PATTERN.matcher(normalizedCode).matches()) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "标签类型编码格式不合法");
        }
        return normalizedCode;
    }

    /**
     * 规范化标签类型名称。
     *
     * @param name 原始名称
     * @return 规范化后的名称
     */
    private String normalizeName(String name) {
        Assert.notEmpty(name, "标签类型名称不能为空");
        String normalizedName = name.trim();
        Assert.notEmpty(normalizedName, "标签类型名称不能为空");
        return normalizedName;
    }

    /**
     * 规范化排序值。
     *
     * @param sort 原始排序值
     * @return 规范化后的排序值
     */
    private Integer normalizeSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    /**
     * 规范化状态值。
     *
     * @param status 原始状态值
     * @return 规范化后的状态值
     */
    private Integer normalizeStatus(Integer status) {
        Assert.notNull(status, "标签类型状态不能为空");
        Set<Integer> supportedStatusSet = Set.of(
                MallProductTagConstants.STATUS_ENABLED,
                MallProductTagConstants.STATUS_DISABLED
        );
        if (!supportedStatusSet.contains(status)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "标签类型状态不合法");
        }
        return status;
    }

    /**
     * 转换为标签类型简要视图。
     *
     * @param type 标签类型实体
     * @return 标签类型视图
     */
    private MallProductTagTypeVo toVo(MallProductTagType type) {
        MallProductTagTypeVo vo = new MallProductTagTypeVo();
        vo.setId(type.getId());
        vo.setCode(type.getCode());
        vo.setName(type.getName());
        return vo;
    }

    /**
     * 转换为标签类型管理视图。
     *
     * @param type 标签类型实体
     * @return 标签类型管理视图
     */
    private MallProductTagTypeAdminVo toAdminVo(MallProductTagType type) {
        MallProductTagTypeAdminVo vo = new MallProductTagTypeAdminVo();
        vo.setId(type.getId());
        vo.setCode(type.getCode());
        vo.setName(type.getName());
        vo.setSort(type.getSort());
        vo.setStatus(type.getStatus());
        vo.setCreateTime(type.getCreateTime());
        vo.setUpdateTime(type.getUpdateTime());
        vo.setCreateBy(type.getCreateBy());
        vo.setUpdateBy(type.getUpdateBy());
        return vo;
    }
}
