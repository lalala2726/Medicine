package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigAddRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigListQueryRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * LLM配置服务接口
 *
 * @author Chuang
 */
public interface LlmConfigService extends IService<LlmConfig> {

    /**
     * 根据ID获取LLM配置
     *
     * @param id 配置ID
     * @return LLM配置
     */
    LlmConfig getLlmConfigById(Long id);

    /**
     * 获取LLM配置列表
     *
     * @param request 查询参数
     * @return LLM配置列表
     */
    Page<LlmConfig> listLlmConfig(LlmConfigListQueryRequest request);

    /**
     * 添加LLM配置
     *
     * @param request 添加请求对象
     * @return 是否添加成功
     */
    boolean addLlmConfig(LlmConfigAddRequest request);

    /**
     * 修改LLM配置
     *
     * @param request 修改请求对象
     * @return 是否修改成功
     */
    boolean updateLlmConfig(LlmConfigUpdateRequest request);

    /**
     * 删除LLM配置
     *
     * @param ids 配置ID列表
     * @return 是否删除成功
     */
    boolean deleteLlmConfig(List<Long> ids);

    
    /**
     * 获取所有启用的LLM配置
     * 用于大模型动态切换功能
     *
     * @return 启用的LLM配置列表
     */
    java.util.List<LlmConfig> getEnabledLlmConfigs();

    /**
     * 根据提供商名称获取LLM配置
     *
     * @param provider 提供商名称
     * @return LLM配置
     */
    LlmConfig getLlmConfigByProvider(String provider);

    /**
     * 刷新配置缓存
     * 当配置发生变化时调用此方法清除缓存
     */
    void refreshConfigCache();

}
