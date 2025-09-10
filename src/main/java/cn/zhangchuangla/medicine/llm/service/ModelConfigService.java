package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.RedisCache;
import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.model.dto.ModelConfigDto;
import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.model.entity.ModelConfig;
import cn.zhangchuangla.medicine.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/9 20:29
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelConfigService {

    public static final String CHAT_CONFIG_KEY = "llm:config";

    private final RedisCache redisCache;

    private final LlmConfigService llmConfigService;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 更新聊天配置
     */
    public void updateChatConfig(ModelConfigDto modelConfigDto) {
        ModelConfig modelConfig = new ModelConfig();
        BeanUtils.copyProperties(modelConfigDto, modelConfig);
        Assert.hasText(modelConfig.getProvider(), "请选择模型提供商");
        Assert.hasText(modelConfig.getModel(), "请选择模型名称");

        LlmConfig llmConfigByProvider = llmConfigService.getLlmConfigByProvider(modelConfig.getProvider());
        Assert.notNull(llmConfigByProvider, "模型厂商不存在!请重新选择!");
        if (!llmConfigByProvider.getModel().contains(modelConfig.getModel())) {
            throw new ServiceException("请选择正确的模型名称");
        }
        BeanUtils.copyProperties(llmConfigByProvider, modelConfig);
        log.info("更新模型配置成功:{}", modelConfig);
        redisCache.setCacheObject(CHAT_CONFIG_KEY, modelConfig);
        // 发布配置更新事件，通知使用方刷新本地缓存/客户端
        try {
            eventPublisher.publishEvent(new ModelConfigUpdatedEvent(this, modelConfig));
        } catch (Exception e) {
            log.warn("发布模型配置更新事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取聊天配置
     *
     * @return 聊天配置
     */
    public ModelConfig getChatConfig() {
        ModelConfig cacheObject = redisCache.getCacheObject(CHAT_CONFIG_KEY);
        if (cacheObject == null) {
            throw new ServiceException("请先设置模型配置");
        }
        return cacheObject;
    }
}
