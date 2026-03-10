package cn.zhangchuangla.medicine.admin.facade;

import cn.zhangchuangla.medicine.admin.model.dto.LlmProviderDetailDto;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderCreateRequest;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderModelCreateRequest;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderModelUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderUpdateRequest;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;

import java.util.List;

/**
 * 大模型提供商聚合门面。
 *
 * @author Chuang
 */
public interface LlmProviderFacade {

    /**
     * 查询提供商详情以及关联模型列表。
     *
     * @param id 提供商ID
     * @return 提供商详情
     */
    LlmProviderDetailDto getProviderDetail(Long id);

    /**
     * 新增提供商以及其关联模型。
     *
     * @param request 新增请求
     * @return 是否新增成功
     */
    boolean createProvider(LlmProviderCreateRequest request);

    /**
     * 编辑提供商以及其关联模型。
     *
     * @param request 编辑请求
     * @return 是否编辑成功
     */
    boolean updateProvider(LlmProviderUpdateRequest request);

    /**
     * 删除提供商以及其关联模型。
     *
     * @param id 提供商ID
     * @return 是否删除成功
     */
    boolean deleteProvider(Long id);

    /**
     * 查询指定提供商下的全部模型。
     *
     * @param providerId 提供商ID
     * @return 模型列表
     */
    List<LlmProviderModel> listProviderModels(Long providerId);

    /**
     * 新增单个模型。
     *
     * @param request 新增模型请求
     * @return 是否新增成功
     */
    boolean createProviderModel(LlmProviderModelCreateRequest request);

    /**
     * 编辑单个模型。
     *
     * @param request 编辑模型请求
     * @return 是否编辑成功
     */
    boolean updateProviderModel(LlmProviderModelUpdateRequest request);
}
