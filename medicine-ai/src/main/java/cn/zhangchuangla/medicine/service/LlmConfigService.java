package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.common.core.common.base.Option;
import cn.zhangchuangla.medicine.common.core.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.common.core.model.request.llm.LlmConfigAddRequest;
import cn.zhangchuangla.medicine.common.core.model.request.llm.LlmConfigListQueryRequest;
import cn.zhangchuangla.medicine.common.core.model.request.llm.LlmConfigUpdateRequest;
import cn.zhangchuangla.medicine.common.core.model.vo.llm.LLMOptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * LLM配置服务接口
 *
 * @author Chuang
 */
public interface LlmConfigService {

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
     * 获取所有LLM配置选项
     *
     * @return LLM配置选项列表
     */
    List<LLMOptions> getLLMOptions();

    /**
     * 获取所有LLM提供商
     *
     * @return LLM提供商列表
     */
    List<Option<String>> getLLMProvider();

    /**
     * 获取所有LLM模型
     *
     * @param provider 提供商名称
     * @return LLM模型列表
     */
    List<Option<String>> getLLMModel(String provider);
}
