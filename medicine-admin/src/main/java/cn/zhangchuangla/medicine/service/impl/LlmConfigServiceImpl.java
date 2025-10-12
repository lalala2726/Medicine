package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.ai.service.LlmConfigService;
import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.mapper.LLMConfigMapper;
import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigAddRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigListQueryRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.llm.LLMOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LLM配置服务实现类
 *
 * @author Chuang
 */
@Slf4j
@Service
public class LlmConfigServiceImpl extends ServiceImpl<LLMConfigMapper, LlmConfig>
        implements LlmConfigService, BaseService {


    /**
     * 启用状态
     */
    private static final int ENABLED_STATUS = 0;

    /**
     * 根据ID获取LLM配置
     *
     * @param id 配置ID
     * @return LLM配置
     */
    @Override
    public LlmConfig getLlmConfigById(Long id) {
        return getById(id);
    }

    /**
     * 获取LLM配置列表
     *
     * @param request 查询参数
     * @return LLM配置列表
     */
    @Override
    public Page<LlmConfig> listLlmConfig(LlmConfigListQueryRequest request) {
        return baseMapper.listLlmConfig(new Page<>(request.getPageNum(), request.getPageSize()), request);
    }


    /**
     * 获取所有启用的LLM配置
     * 用于大模型动态切换功能
     *
     * @return 启用的LLM配置列表
     */
    @Override
    public List<LlmConfig> getEnabledLlmConfigs() {
        return getEnabledLlmConfigsFromDatabase();
    }

    /**
     * 根据提供商名称获取LLM配置
     *
     * @param provider 提供商名称
     * @return LLM配置
     */
    @Override
    public LlmConfig getLlmConfigByProvider(String provider) {
        return lambdaQuery()
                .eq(LlmConfig::getStatus, ENABLED_STATUS)
                .eq(LlmConfig::getProvider, provider)
                .orderByDesc(LlmConfig::getCreateTime)
                .one();
    }

    /**
     * 获取所有LLM配置选项
     *
     * @return LLM配置选项列表
     */
    @Override
    @Deprecated
    public List<LLMOptions> getLLMOptions() {
        LambdaQueryWrapper<LlmConfig> queryWrapper = new LambdaQueryWrapper<LlmConfig>()
                .eq(LlmConfig::getStatus, ENABLED_STATUS);

        List<LlmConfig> list = list(queryWrapper);

        // 按provider分组，合并同一个provider下的所有模型
        Map<String, List<String>> providerModelsMap = list.stream()
                .collect(Collectors.groupingBy(
                        LlmConfig::getProvider,
                        Collectors.mapping(
                                config -> {
                                    String[] models = config.getModel().split(",");
                                    return List.of(models);
                                },
                                Collectors.reducing(new ArrayList<>(), (list1, list2) -> {
                                    List<String> merged = new ArrayList<>(list1);
                                    merged.addAll(list2);
                                    return merged;
                                })
                        )
                ));

        // 转换为LLMOptions列表
        return providerModelsMap.entrySet().stream()
                .map(entry -> {
                    LLMOptions options = new LLMOptions();
                    options.setProvider(entry.getKey());
                    // 去重并排序模型列表
                    List<String> uniqueModels = entry.getValue().stream()
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());
                    options.setModel(uniqueModels);
                    return options;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取LLM提供商选项
     *
     * @return LLM提供商选项列表
     */
    @Override
    public List<Option<String>> getLLMProvider() {
        try {
            List<LlmConfig> configs = lambdaQuery()
                    .eq(LlmConfig::getStatus, ENABLED_STATUS)
                    .eq(LlmConfig::getIsDelete, 0)
                    .select(LlmConfig::getProvider)
                    .list();

            return configs.stream()
                    .map(config -> new Option<>(config.getProvider(), config.getProvider()))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取LLM提供商列表失败", e);
            return List.of();
        }
    }

    /**
     * 获取LLM模型选项
     *
     * @param provider 提供商名称
     * @return LLM模型选项列表
     */
    @Override
    public List<Option<String>> getLLMModel(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            return List.of();
        }

        try {
            List<LlmConfig> configs = lambdaQuery()
                    .eq(LlmConfig::getStatus, ENABLED_STATUS)
                    .eq(LlmConfig::getIsDelete, 0)
                    .eq(LlmConfig::getProvider, provider)
                    .select(LlmConfig::getModel)
                    .list();

            return configs.stream()
                    .flatMap(config -> Arrays.stream(config.getModel().split(",")))
                    .map(String::trim)
                    .filter(model -> !model.isEmpty())
                    .map(model -> new Option<>(model, model))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取LLM模型列表失败，provider: {}", provider, e);
            return List.of();
        }
    }


    /**
     * 从数据库获取所有启用的配置
     */
    private List<LlmConfig> getEnabledLlmConfigsFromDatabase() {
        try {
            return lambdaQuery()
                    .eq(LlmConfig::getStatus, ENABLED_STATUS)
                    .orderByDesc(LlmConfig::getCreateTime)
                    .list();
        } catch (Exception e) {
            log.error("从数据库获取启用配置失败", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 添加LLM配置
     *
     * @param request 添加请求对象
     * @return 是否添加成功
     */
    @Override
    public boolean addLlmConfig(LlmConfigAddRequest request) {
        return doAddLlmConfig(request);
    }

    /**
     * 修改LLM配置
     *
     * @param request 修改请求对象
     * @return 是否修改成功
     */
    @Override
    public boolean updateLlmConfig(LlmConfigUpdateRequest request) {
        return doUpdateLlmConfig(request);
    }

    /**
     * 删除LLM配置
     *
     * @param ids 配置ID列表
     * @return 是否删除成功
     */
    @Override
    public boolean deleteLlmConfig(List<Long> ids) {
        return doDeleteLlmConfig(ids);
    }

    /**
     * 内部添加配置方法
     */
    private boolean doAddLlmConfig(LlmConfigAddRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.notEmpty(request.getProvider(), "模型提供商不能为空");
        Assert.notEmpty(request.getModel(), "模型不能为空");
        Assert.notEmpty(request.getApiKey(), "API KEY不能为空");

        // 检查提供商唯一性
        validateProviderUniqueness(request.getProvider(), null);

        // 检查提供商和模型组合唯一性
        validateProviderModelUniqueness(request.getProvider(), request.getModel(), null);

        LlmConfig llmConfig = new LlmConfig();
        BeanUtils.copyProperties(request, llmConfig);
        // 将List<String>转换为String存储
        if (request.getModel() != null && !request.getModel().isEmpty()) {
            llmConfig.setModel(String.join(",", request.getModel()));
        }


        // 设置创建信息
        llmConfig.setCreateBy(getUsername());
        return save(llmConfig);
    }

    /**
     * 内部修改配置方法
     */
    private boolean doUpdateLlmConfig(LlmConfigUpdateRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.notNull(request.getId(), "配置ID不能为空");

        LlmConfig existingConfig = getById(request.getId());
        Assert.notNull(existingConfig, "配置不存在");

        // 检查提供商唯一性（排除当前记录）
        validateProviderUniqueness(request.getProvider(), request.getId());

        // 检查提供商和模型组合唯一性（排除当前记录）
        validateProviderModelUniqueness(request.getProvider(), request.getModel(), request.getId());

        BeanUtils.copyProperties(request, existingConfig);
        // 将List<String>转换为String存储
        if (request.getModel() != null && !request.getModel().isEmpty()) {
            existingConfig.setModel(String.join(",", request.getModel()));
        }
        existingConfig.setUpdateTime(new Date());

        return updateById(existingConfig);
    }

    /**
     * 内部删除配置方法
     */
    private boolean doDeleteLlmConfig(List<Long> ids) {
        Assert.notNull(ids, "配置ID列表不能为空");
        Assert.notEmpty(ids, "配置ID列表不能为空");

        // 逻辑删除
        return lambdaUpdate()
                .set(LlmConfig::getDeleteTime, new Date())
                .in(LlmConfig::getId, ids)
                .update();
    }

    /**
     * 验证提供商唯一性
     *
     * @param provider  提供商名称
     * @param excludeId 排除的配置ID（用于更新时排除自身）
     */
    private void validateProviderUniqueness(String provider, Long excludeId) {
        LambdaQueryChainWrapper<LlmConfig> queryWrapper = lambdaQuery()
                .eq(LlmConfig::getProvider, provider)
                .eq(LlmConfig::getIsDelete, 0);

        if (excludeId != null) {
            queryWrapper.ne(LlmConfig::getId, excludeId);
        }

        LlmConfig existingConfig = queryWrapper.one();
        if (existingConfig != null) {
            throw new IllegalArgumentException("提供商 '" + provider + "' 已存在，请勿重复添加");
        }
    }

    /**
     * 验证提供商和模型组合唯一性
     *
     * @param provider  提供商名称
     * @param models    模型列表
     * @param excludeId 排除的配置ID（用于更新时排除自身）
     */
    private void validateProviderModelUniqueness(String provider, List<String> models, Long excludeId) {
        if (models == null || models.isEmpty()) {
            return;
        }

        // 获取当前提供商的所有现有配置
        LambdaQueryChainWrapper<LlmConfig> queryWrapper = lambdaQuery()
                .eq(LlmConfig::getProvider, provider)
                .eq(LlmConfig::getIsDelete, 0);

        if (excludeId != null) {
            queryWrapper.ne(LlmConfig::getId, excludeId);
        }

        List<LlmConfig> existingConfigs = queryWrapper.list();

        // 检查模型重复
        for (String model : models) {
            for (LlmConfig existingConfig : existingConfigs) {
                String[] existingModels = existingConfig.getModel().split(",");
                for (String existingModel : existingModels) {
                    if (model.trim().equals(existingModel.trim())) {
                        throw new IllegalArgumentException("提供商 '" + provider + "' 中模型 '" + model + "' 已存在");
                    }
                }
            }
        }
    }

}




