package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.base.BaseService;
import cn.zhangchuangla.medicine.common.redis.RedisCache;
import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.mapper.LLMConfigMapper;
import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigAddRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigListQueryRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigUpdateRequest;
import cn.zhangchuangla.medicine.service.LlmConfigService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * LLM配置服务实现类
 *
 * @author Chuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigServiceImpl extends ServiceImpl<LLMConfigMapper, LlmConfig>
        implements LlmConfigService, BaseService {


    /**
     * 启用状态
     */
    private static final int ENABLED_STATUS = 0;



    /**
     * 缓存键前缀
     */
    private static final String LLM_ENABLED_CONFIGS_KEY = "llm:config:enabled";
    private static final String LLM_CONFIG_PROVIDER_PREFIX = "llm:config:provider:";
    /**
     * 缓存过期时间（5分钟）
     */
    private static final long CACHE_EXPIRE_TIME = 5 * 60;
    private final RedisCache redisCache;

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
     * 用于大模型动态切换功能，带缓存机制
     *
     * @return 启用的LLM配置列表
     */
    @Override
    public List<LlmConfig> getEnabledLlmConfigs() {
        try {
            // 从Redis缓存获取
            List<LlmConfig> enabledConfigs = redisCache.getCacheObject(LLM_ENABLED_CONFIGS_KEY);
            if (enabledConfigs == null || enabledConfigs.isEmpty()) {
                refreshConfigCache();
                enabledConfigs = redisCache.getCacheObject(LLM_ENABLED_CONFIGS_KEY);
            }
            return enabledConfigs != null ? enabledConfigs : new java.util.ArrayList<>();
        } catch (Exception e) {
            log.warn("Redis缓存获取失败，直接查询数据库: {}", e.getMessage());
            return getEnabledLlmConfigsFromDatabase();
        }
    }

    /**
     * 根据提供商名称获取LLM配置
     * 带缓存机制，提高查询性能
     *
     * @param provider 提供商名称
     * @return LLM配置
     */
    @Override
    public LlmConfig getLlmConfigByProvider(String provider) {
        try {
            // 从Redis缓存获取
            String providerKey = LLM_CONFIG_PROVIDER_PREFIX + provider;
            LlmConfig config = redisCache.getCacheObject(providerKey);
            if (config == null) {
                refreshConfigCache();
                config = redisCache.getCacheObject(providerKey);
            }
            return config;
        } catch (Exception e) {
            log.warn("Redis缓存获取失败，直接查询数据库: {}", e.getMessage());
            return getLlmConfigByProviderFromDatabase(provider);
        }
    }


    /**
     * 从数据库获取所有启用的配置（Redis不可用时的备用方案）
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
     * 从数据库根据提供商获取配置（Redis不可用时的备用方案）
     */
    private LlmConfig getLlmConfigByProviderFromDatabase(String provider) {
        try {
            return lambdaQuery()
                    .eq(LlmConfig::getStatus, 1)
                    .eq(LlmConfig::getIsDelete, 0)
                    .eq(LlmConfig::getProvider, provider)
                    .orderByDesc(LlmConfig::getCreateTime)
                    .one();
        } catch (Exception e) {
            log.error("从数据库获取提供商配置失败: {}", provider, e);
            return null;
        }
    }

    /**
     * 刷新配置缓存
     * 当配置发生变化时调用此方法清除缓存
     */
    @Override
    public void refreshConfigCache() {
        try {
            // 查询所有启用的配置
            LambdaQueryChainWrapper<LlmConfig> queryWrapper = lambdaQuery()
                    .eq(LlmConfig::getStatus, ENABLED_STATUS)
                    .orderByDesc(LlmConfig::getCreateTime);

            List<LlmConfig> enabledConfigs = queryWrapper.list();

            // 清除旧的缓存
            redisCache.deleteObject(LLM_ENABLED_CONFIGS_KEY);

            // 删除所有提供商相关的缓存
            List<String> providerKeys = redisCache.scanKeys(LLM_CONFIG_PROVIDER_PREFIX + "*");
            if (!providerKeys.isEmpty()) {
                redisCache.deleteObject(providerKeys);
            }

            // 重新填充缓存
            for (LlmConfig config : enabledConfigs) {
                // 缓存每个提供商的配置
                String providerKey = LLM_CONFIG_PROVIDER_PREFIX + config.getProvider();
                redisCache.setCacheObject(providerKey, config, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            }

            // 缓存所有启用的配置列表
            redisCache.setCacheObject(LLM_ENABLED_CONFIGS_KEY, enabledConfigs, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);

            log.info("刷新LLM配置缓存成功，共缓存{}个配置", enabledConfigs.size());

        } catch (Exception e) {
            // 缓存刷新失败时记录日志，但不影响业务
            log.error("刷新LLM配置缓存失败", e);
        }
    }

    /**
     * 添加LLM配置并刷新缓存
     *
     * @param request 添加请求对象
     * @return 是否添加成功
     */
    @Override
    public boolean addLlmConfig(LlmConfigAddRequest request) {
        boolean result = doAddLlmConfig(request);
        if (result) {
            refreshConfigCache(); // 添加成功后刷新缓存
        }
        return result;
    }

    /**
     * 修改LLM配置并刷新缓存
     *
     * @param request 修改请求对象
     * @return 是否修改成功
     */
    @Override
    public boolean updateLlmConfig(LlmConfigUpdateRequest request) {
        boolean result = doUpdateLlmConfig(request);
        if (result) {
            refreshConfigCache(); // 修改成功后刷新缓存
        }
        return result;
    }

    /**
     * 删除LLM配置并刷新缓存
     *
     * @param ids 配置ID列表
     * @return 是否删除成功
     */
    @Override
    public boolean deleteLlmConfig(List<Long> ids) {
        boolean result = doDeleteLlmConfig(ids);
        if (result) {
            refreshConfigCache(); // 删除成功后刷新缓存
        }
        return result;
    }

    /**
     * 内部添加配置方法（不刷新缓存）
     */
    private boolean doAddLlmConfig(LlmConfigAddRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.notEmpty(request.getProvider(), "模型提供商不能为空");
        Assert.notEmpty(request.getModel(), "模型不能为空");
        Assert.notEmpty(request.getApiKey(), "API KEY不能为空");

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
     * 内部修改配置方法（不刷新缓存）
     */
    private boolean doUpdateLlmConfig(LlmConfigUpdateRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.notNull(request.getId(), "配置ID不能为空");

        LlmConfig existingConfig = getById(request.getId());
        Assert.notNull(existingConfig, "配置不存在");


        BeanUtils.copyProperties(request, existingConfig);
        // 将List<String>转换为String存储
        if (request.getModel() != null && !request.getModel().isEmpty()) {
            existingConfig.setModel(String.join(",", request.getModel()));
        }
        existingConfig.setUpdateTime(new Date());

        return updateById(existingConfig);
    }

    /**
     * 内部删除配置方法（不刷新缓存）
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

}




