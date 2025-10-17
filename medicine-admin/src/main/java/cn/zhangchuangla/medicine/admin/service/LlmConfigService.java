package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigAddRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigListQueryRequest;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.llm.LLMOptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * LLM 配置管理接口，提供 CRUD 及选项查询能力。
 *
 * @author Chuang
 */
public interface LlmConfigService {

    /**
     * 根据 ID 获取配置详情。
     *
     * @param id 配置主键
     * @return 配置详情
     */
    LlmConfig getLlmConfigById(Long id);

    /**
     * 分页查询配置列表。
     *
     * @param request 查询参数
     * @return 配置分页数据
     */
    Page<LlmConfig> listLlmConfig(LlmConfigListQueryRequest request);

    /**
     * 新增配置。
     *
     * @param request 新增参数
     * @return 是否成功
     */
    boolean addLlmConfig(LlmConfigAddRequest request);

    /**
     * 修改配置。
     *
     * @param request 修改参数
     * @return 是否成功
     */
    boolean updateLlmConfig(LlmConfigUpdateRequest request);

    /**
     * 删除配置。
     *
     * @param ids 待删除主键列表
     * @return 是否成功
     */
    boolean deleteLlmConfig(List<Long> ids);

    /**
     * 查询所有启用的配置。
     *
     * @return 启用配置集合
     */
    List<LlmConfig> getEnabledLlmConfigs();

    /**
     * 根据提供商查询配置。
     *
     * @param provider 提供商名称
     * @return 匹配配置
     */
    LlmConfig getLlmConfigByProvider(String provider);

    /**
     * 查询提供商与模型的组合选项。
     *
     * @return LLM 选项
     */
    List<LLMOptions> getLLMOptions();

    /**
     * 查询可用提供商列表。
     *
     * @return 提供商选项
     */
    List<Option<String>> getLLMProvider();

    /**
     * 查询指定提供商下的模型列表。
     *
     * @param provider 提供商名称
     * @return 模型选项
     */
    List<Option<String>> getLLMModel(String provider);
}
