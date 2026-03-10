package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.LlmProviderConnectivityClient;
import cn.zhangchuangla.medicine.admin.mapper.LlmProviderMapper;
import cn.zhangchuangla.medicine.admin.model.dto.LlmPresetProviderTemplateDto;
import cn.zhangchuangla.medicine.admin.model.dto.LlmProviderListDto;
import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.LlmProviderConnectivityTestVo;
import cn.zhangchuangla.medicine.admin.service.LlmProviderService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import cn.zhangchuangla.medicine.common.http.exception.HttpClientException;
import cn.zhangchuangla.medicine.common.http.model.HttpResult;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 大模型提供商服务实现。
 */
@Service
@RequiredArgsConstructor
public class LlmProviderServiceImpl extends ServiceImpl<LlmProviderMapper, LlmProvider>
        implements LlmProviderService, BaseService {

    private static final String PRESET_PROVIDER_RESOURCE_PATH = "llm/preset-providers.json";
    private static final TypeReference<List<LlmPresetProviderTemplateDto>> PRESET_PROVIDER_LIST_TYPE =
            new TypeReference<>() {
            };
    private static final String DEFAULT_OPERATOR = "system";
    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;
    private static final String PROVIDER_NAME_DUPLICATE_MESSAGE = "提供商名称已存在";
    private static final String ENABLED_PROVIDER_CONFLICT_MESSAGE = "启用提供商只允许存在一个";
    private static final String SINGLE_ENABLED_INDEX_NAME = "uk_llm_provider_single_enabled";
    private static final String SINGLE_ENABLED_GUARD_COLUMN = "enabled_unique_guard";
    private static final String MODELS_PATH_SUFFIX = "/models";
    private static final String CONNECTIVITY_SUCCESS_MESSAGE = "连通成功";
    private static final String OPENAI_FORMAT_INVALID_MESSAGE = "接口返回不符合 OpenAI 兼容格式";
    private static final String NETWORK_FAILURE_MESSAGE = "网络连接失败";
    private static final String INVALID_BASE_URL_MESSAGE = "BaseURL 格式不正确";

    private final LlmProviderMapper llmProviderMapper;
    private final ObjectMapper objectMapper;
    private final LlmProviderConnectivityClient llmProviderConnectivityClient;

    private volatile boolean presetProvidersLoaded;
    private volatile List<LlmPresetProviderTemplateDto> presetProviders = List.of();
    private volatile Map<String, LlmPresetProviderTemplateDto> presetProviderMap = Map.of();

    /**
     * 查询全部预设模型厂商模板。
     *
     * @return 预设模型厂商模板列表
     */
    @Override
    public List<LlmPresetProviderTemplateDto> listPresetProviders() {
        loadPresetProvidersIfNecessary();
        return deepCopyProviders(presetProviders);
    }

    /**
     * 根据厂商英文键查询预设模型厂商模板。
     *
     * @param providerKey 预设厂商英文键
     * @return 预设模型厂商模板详情
     */
    @Override
    public LlmPresetProviderTemplateDto getPresetProvider(String providerKey) {
        return getPresetProviderTemplateOrThrow(providerKey);
    }

    /**
     * 分页查询提供商列表。
     *
     * @param request 查询参数
     * @return 提供商分页结果
     */
    @Override
    public Page<LlmProviderListDto> listProviders(LlmProviderListRequest request) {
        Assert.notNull(request, "查询参数不能为空");
        request.setProviderName(normalizeNullableText(request.getProviderName()));
        Page<LlmProviderListDto> page = request.toPage();
        return llmProviderMapper.listProviders(page, request);
    }

    /**
     * 测试 OpenAI 兼容提供商连通性。
     *
     * @param request 测试请求
     * @return 测试结果
     */
    @Override
    public LlmProviderConnectivityTestVo testConnectivity(LlmProviderConnectivityTestRequest request) {
        Assert.notNull(request, "测试参数不能为空");
        String baseUrl = normalizeNullableText(request.getBaseUrl());
        String apiKey = normalizeNullableText(request.getApiKey());
        Assert.notEmpty(baseUrl, "基础地址不能为空");
        Assert.notEmpty(apiKey, "API Key不能为空");

        String endpoint = buildConnectivityEndpoint(baseUrl);
        long start = System.nanoTime();
        try {
            HttpResult<String> result = llmProviderConnectivityClient.getModels(endpoint, apiKey);
            long latencyMs = elapsedMilliseconds(start);
            return buildConnectivityResult(endpoint, latencyMs, result);
        } catch (HttpClientException ex) {
            long latencyMs = elapsedMilliseconds(start);
            return LlmProviderConnectivityTestVo.builder()
                    .success(false)
                    .httpStatus(null)
                    .endpoint(endpoint)
                    .latencyMs(latencyMs)
                    .message(resolveConnectivityExceptionMessage(ex))
                    .build();
        }
    }

    /**
     * 查询必填提供商实体，不存在时抛出业务异常。
     *
     * @param id 提供商ID
     * @return 提供商实体
     */
    @Override
    public LlmProvider getRequiredProvider(Long id) {
        Assert.isPositive(id, "提供商ID必须大于0");
        LlmProvider provider = llmProviderMapper.selectById(id);
        if (provider == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "提供商不存在");
        }
        return provider;
    }

    /**
     * 新增提供商配置。
     *
     * @param request 新增请求参数
     * @return 新增后的提供商实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlmProvider createProvider(LlmProviderCreateRequest request) {
        Assert.notNull(request, "提供商信息不能为空");

        ProviderResolved resolved = resolveProviderForCreate(request);
        validateProviderNameUnique(resolved.providerName(), null);

        LlmProvider provider = buildCreateProviderEntity(request, resolved, currentOperator());
        if (llmProviderMapper.insert(provider) <= 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存提供商失败");
        }
        return provider;
    }

    /**
     * 编辑提供商配置。
     *
     * @param request 编辑请求参数
     * @return 编辑后的提供商实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlmProvider updateProvider(LlmProviderUpdateRequest request) {
        Assert.notNull(request, "提供商信息不能为空");

        LlmProvider existing = getRequiredProvider(request.getId());
        ProviderResolved resolved = resolveProviderForUpdate(existing, request);
        validateProviderNameUnique(resolved.providerName(), existing.getId());

        LlmProvider provider = buildUpdateProviderEntity(existing, request, resolved, currentOperator());
        updateProviderById(provider);
        return provider;
    }

    /**
     * 更新提供商 API Key。
     *
     * @param request API Key 修改请求
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProviderApiKey(LlmProviderApiKeyUpdateRequest request) {
        Assert.notNull(request, "API Key修改参数不能为空");

        LlmProvider existing = getRequiredProvider(request.getId());
        String apiKey = normalizeNullableText(request.getApiKey());
        Assert.notEmpty(apiKey, "API Key不能为空");

        LlmProvider provider = LlmProvider.builder()
                .id(existing.getId())
                .apiKey(apiKey)
                .updateBy(currentOperator())
                .build();
        updateProviderById(provider);
        return true;
    }

    /**
     * 删除提供商配置。
     *
     * @param id 提供商ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProvider(Long id) {
        getRequiredProvider(id);
        return llmProviderMapper.deleteById(id) > 0;
    }

    /**
     * 根据新增请求解析提供商最终配置。
     *
     * @param request 新增请求
     * @return 归一化后的提供商信息
     */
    private ProviderResolved resolveProviderForCreate(LlmProviderCreateRequest request) {
        String providerName = normalizeNullableText(request.getProviderName());
        String baseUrl = normalizeNullableText(request.getBaseUrl());
        String description = normalizeNullableText(request.getDescription());

        Assert.notEmpty(providerName, "提供商名称不能为空");
        Assert.notEmpty(baseUrl, "基础地址不能为空");
        return new ProviderResolved(providerName, baseUrl, description);
    }

    /**
     * 根据编辑请求解析提供商最终配置。
     *
     * @param existing 已存在的提供商实体
     * @param request  编辑请求
     * @return 归一化后的提供商信息
     */
    private ProviderResolved resolveProviderForUpdate(LlmProvider existing, LlmProviderUpdateRequest request) {
        String providerName = normalizeNullableText(request.getProviderName(), existing.getProviderName());
        String baseUrl = normalizeNullableText(request.getBaseUrl(), existing.getBaseUrl());
        String description = request.getDescription() == null
                ? normalizeNullableText(existing.getDescription())
                : normalizeNullableText(request.getDescription());

        Assert.notEmpty(providerName, "提供商名称不能为空");
        Assert.notEmpty(baseUrl, "基础地址不能为空");
        return new ProviderResolved(providerName, baseUrl, description);
    }

    /**
     * 构建新增提供商实体。
     *
     * @param request  新增请求
     * @param resolved 已解析的提供商配置
     * @param operator 操作人
     * @return 提供商实体
     */
    private LlmProvider buildCreateProviderEntity(LlmProviderCreateRequest request,
                                                  ProviderResolved resolved,
                                                  String operator) {
        return LlmProvider.builder()
                .providerName(resolved.providerName())
                .baseUrl(resolved.baseUrl())
                .apiKey(normalizeNullableText(request.getApiKey()))
                .description(resolved.description())
                .status(resolveCreateProviderStatus())
                .sort(defaultSort(request.getSort()))
                .createBy(operator)
                .updateBy(operator)
                .build();
    }

    /**
     * 构建编辑提供商实体。
     *
     * @param existing 已存在的提供商实体
     * @param request  编辑请求
     * @param resolved 已解析的提供商配置
     * @param operator 操作人
     * @return 提供商实体
     */
    private LlmProvider buildUpdateProviderEntity(LlmProvider existing,
                                                  LlmProviderUpdateRequest request,
                                                  ProviderResolved resolved,
                                                  String operator) {
        return LlmProvider.builder()
                .id(existing.getId())
                .providerName(resolved.providerName())
                .baseUrl(resolved.baseUrl())
                .apiKey(existing.getApiKey())
                .description(resolved.description())
                .status(existing.getStatus())
                .sort(request.getSort() == null ? defaultSort(existing.getSort()) : defaultSort(request.getSort()))
                .updateBy(operator)
                .build();
    }

    /**
     * 更新提供商状态。
     *
     * @param request 状态修改请求
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProviderStatus(LlmProviderUpdateStatusRequest request) {
        Assert.notNull(request, "状态修改参数不能为空");

        LlmProvider existing = getRequiredProvider(request.getId());
        String operator = currentOperator();
        Integer targetStatus = request.getStatus();
        if (targetStatus == STATUS_ENABLED) {
            disableOtherEnabledProviders(existing.getId(), operator);
        }

        LlmProvider provider = LlmProvider.builder()
                .id(existing.getId())
                .status(targetStatus)
                .updateBy(operator)
                .build();
        updateProviderById(provider);
        return true;
    }

    /**
     * 更新提供商实体。
     *
     * @param provider 提供商实体
     */
    private void updateProviderById(LlmProvider provider) {
        if (llmProviderMapper.updateById(provider) <= 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "更新提供商失败");
        }
    }

    /**
     * 校验提供商名称唯一性。
     *
     * @param providerName 提供商名称
     * @param excludeId    排除的提供商ID
     */
    private void validateProviderNameUnique(String providerName, Long excludeId) {
        Long count = llmProviderMapper.selectCount(Wrappers.<LlmProvider>lambdaQuery()
                .eq(LlmProvider::getProviderName, providerName)
                .ne(excludeId != null, LlmProvider::getId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, PROVIDER_NAME_DUPLICATE_MESSAGE);
        }
    }

    /**
     * 构建提供商连通性测试地址。
     *
     * @param baseUrl 基础地址
     * @return 实际请求地址
     */
    private String buildConnectivityEndpoint(String baseUrl) {
        String endpoint = baseUrl;
        while (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        Assert.notEmpty(endpoint, "基础地址不能为空");
        return endpoint.endsWith(MODELS_PATH_SUFFIX) ? endpoint : endpoint + MODELS_PATH_SUFFIX;
    }

    /**
     * 构建连通性测试结果。
     *
     * @param endpoint  实际请求地址
     * @param latencyMs 请求耗时
     * @param result    HTTP 响应结果
     * @return 测试结果
     */
    private LlmProviderConnectivityTestVo buildConnectivityResult(String endpoint,
                                                                  long latencyMs,
                                                                  HttpResult<String> result) {
        int httpStatus = result.getStatusCode();
        if (httpStatus == 200 && isOpenAiModelsResponse(result.getBody())) {
            return LlmProviderConnectivityTestVo.builder()
                    .success(true)
                    .httpStatus(httpStatus)
                    .endpoint(endpoint)
                    .latencyMs(latencyMs)
                    .message(CONNECTIVITY_SUCCESS_MESSAGE)
                    .build();
        }
        return LlmProviderConnectivityTestVo.builder()
                .success(false)
                .httpStatus(httpStatus)
                .endpoint(endpoint)
                .latencyMs(latencyMs)
                .message(resolveConnectivityFailureMessage(httpStatus, result.getBody()))
                .build();
    }

    /**
     * 判断是否为 OpenAI 兼容的 models 响应。
     *
     * @param responseBody 响应内容
     * @return 是否兼容
     */
    private boolean isOpenAiModelsResponse(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return false;
        }
        try {
            JsonElement element = JSONUtils.parse(responseBody);
            if (!element.isJsonObject()) {
                return false;
            }
            JsonObject object = element.getAsJsonObject();
            JsonElement data = object.get("data");
            return data != null && data.isJsonArray();
        } catch (JsonParseException | IllegalStateException ex) {
            return false;
        }
    }

    /**
     * 解析连通性失败提示。
     *
     * @param httpStatus   HTTP 状态码
     * @param responseBody 响应内容
     * @return 失败提示
     */
    private String resolveConnectivityFailureMessage(int httpStatus, String responseBody) {
        if (httpStatus == 200 && !isOpenAiModelsResponse(responseBody)) {
            return OPENAI_FORMAT_INVALID_MESSAGE;
        }
        return switch (httpStatus) {
            case 401 -> "API Key 无效或已过期";
            case 403 -> "当前 API Key 无访问权限";
            case 404 -> "BaseURL 不正确或目标服务不是 OpenAI 兼容接口";
            case 429 -> "请求受限或额度不足";
            default -> "连通失败，HTTP 状态码: " + httpStatus;
        };
    }

    /**
     * 解析连通性测试异常提示。
     *
     * @param ex HTTP 异常
     * @return 异常提示
     */
    private String resolveConnectivityExceptionMessage(HttpClientException ex) {
        String message = ex.getMessage();
        if (StringUtils.hasText(message) && message.startsWith("Invalid url:")) {
            return INVALID_BASE_URL_MESSAGE;
        }
        return NETWORK_FAILURE_MESSAGE;
    }

    /**
     * 计算耗时毫秒值。
     *
     * @param start 开始时间
     * @return 耗时毫秒
     */
    private long elapsedMilliseconds(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    /**
     * 根据英文键查询预设模板，不存在时抛出异常。
     *
     * @param providerKey 预设厂商英文键
     * @return 预设模板详情
     */
    private LlmPresetProviderTemplateDto getPresetProviderTemplateOrThrow(String providerKey) {
        loadPresetProvidersIfNecessary();
        String normalizedProviderKey = normalizePresetProviderKey(providerKey);
        Assert.notEmpty(normalizedProviderKey, "预设模型厂商英文键不能为空");
        LlmPresetProviderTemplateDto template = presetProviderMap.get(normalizedProviderKey);
        if (template == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "预设模型厂商不存在");
        }
        return deepCopyProvider(template);
    }

    /**
     * 懒加载预设厂商配置。
     */
    private void loadPresetProvidersIfNecessary() {
        if (presetProvidersLoaded) {
            return;
        }
        synchronized (this) {
            if (presetProvidersLoaded) {
                return;
            }
            ClassPathResource resource = new ClassPathResource(PRESET_PROVIDER_RESOURCE_PATH);
            if (!resource.exists()) {
                throw new IllegalStateException("预设模型厂商资源不存在: " + PRESET_PROVIDER_RESOURCE_PATH);
            }
            try (InputStream inputStream = resource.getInputStream()) {
                List<LlmPresetProviderTemplateDto> providerList = objectMapper.readValue(inputStream, PRESET_PROVIDER_LIST_TYPE);
                List<LlmPresetProviderTemplateDto> copiedProviders = deepCopyProviders(providerList);
                this.presetProviders = copiedProviders;
                this.presetProviderMap = buildPresetProviderMap(copiedProviders);
                this.presetProvidersLoaded = true;
            } catch (IOException ex) {
                throw new IllegalStateException("读取预设模型厂商资源失败: " + PRESET_PROVIDER_RESOURCE_PATH, ex);
            }
        }
    }

    /**
     * 构建预设厂商缓存映射。
     *
     * @param providers 预设厂商列表
     * @return 厂商映射
     */
    private Map<String, LlmPresetProviderTemplateDto> buildPresetProviderMap(List<LlmPresetProviderTemplateDto> providers) {
        if (providers == null || providers.isEmpty()) {
            return Map.of();
        }
        Map<String, LlmPresetProviderTemplateDto> providerMap = new LinkedHashMap<>();
        for (LlmPresetProviderTemplateDto provider : providers) {
            if (provider == null || !StringUtils.hasText(provider.getProviderKey())) {
                continue;
            }
            providerMap.put(normalizePresetProviderKey(provider.getProviderKey()), deepCopyProvider(provider));
        }
        return Map.copyOf(providerMap);
    }

    /**
     * 深拷贝预设厂商列表。
     *
     * @param providers 原始预设厂商列表
     * @return 拷贝后的预设厂商列表
     */
    private List<LlmPresetProviderTemplateDto> deepCopyProviders(List<LlmPresetProviderTemplateDto> providers) {
        if (providers == null || providers.isEmpty()) {
            return List.of();
        }
        List<LlmPresetProviderTemplateDto> copies = new ArrayList<>(providers.size());
        for (LlmPresetProviderTemplateDto provider : providers) {
            if (provider != null) {
                copies.add(deepCopyProvider(provider));
            }
        }
        return List.copyOf(copies);
    }

    /**
     * 深拷贝单个预设厂商。
     *
     * @param source 原始预设厂商
     * @return 拷贝后的预设厂商
     */
    private LlmPresetProviderTemplateDto deepCopyProvider(LlmPresetProviderTemplateDto source) {
        return LlmPresetProviderTemplateDto.builder()
                .providerKey(source.getProviderKey())
                .providerName(source.getProviderName())
                .baseUrl(source.getBaseUrl())
                .description(source.getDescription())
                .models(deepCopyPresetModels(source.getModels()))
                .build();
    }

    /**
     * 深拷贝预设模型列表。
     *
     * @param models 原始预设模型列表
     * @return 拷贝后的预设模型列表
     */
    private List<LlmPresetProviderTemplateDto.Model> deepCopyPresetModels(List<LlmPresetProviderTemplateDto.Model> models) {
        if (models == null || models.isEmpty()) {
            return List.of();
        }
        List<LlmPresetProviderTemplateDto.Model> copies = new ArrayList<>(models.size());
        for (LlmPresetProviderTemplateDto.Model model : models) {
            if (model == null) {
                continue;
            }
            copies.add(LlmPresetProviderTemplateDto.Model.builder()
                    .modelName(model.getModelName())
                    .modelType(model.getModelType())
                    .supportReasoning(model.getSupportReasoning())
                    .supportVision(model.getSupportVision())
                    .description(model.getDescription())
                    .enabled(model.getEnabled())
                    .sort(model.getSort())
                    .build());
        }
        return List.copyOf(copies);
    }

    /**
     * 归一化预设厂商英文键。
     *
     * @param providerKey 厂商英文键
     * @return 归一化后的英文键
     */
    private String normalizePresetProviderKey(String providerKey) {
        String normalizedProviderKey = normalizeNullableText(providerKey);
        return normalizedProviderKey == null ? null : normalizedProviderKey.toLowerCase(Locale.ROOT);
    }

    /**
     * 解析新增提供商时的默认状态。
     *
     * @return 当不存在启用中的提供商时返回启用状态，否则返回停用状态
     */
    private Integer resolveCreateProviderStatus() {
        return countEnabledProviders() > 0 ? STATUS_DISABLED : STATUS_ENABLED;
    }

    /**
     * 统计当前启用中的提供商数量。
     *
     * @return 启用中的提供商数量
     */
    private Long countEnabledProviders() {
        Long count = llmProviderMapper.selectCount(Wrappers.<LlmProvider>lambdaQuery()
                .eq(LlmProvider::getStatus, STATUS_ENABLED));
        return count == null ? 0L : count;
    }

    /**
     * 将除目标提供商外的其他启用项全部停用。
     *
     * @param providerId 目标提供商ID
     * @param operator   操作人
     */
    private void disableOtherEnabledProviders(Long providerId, String operator) {
        llmProviderMapper.update(LlmProvider.builder()
                        .status(STATUS_DISABLED)
                        .updateBy(operator)
                        .build(),
                Wrappers.<LlmProvider>lambdaUpdate()
                        .ne(LlmProvider::getId, providerId)
                        .eq(LlmProvider::getStatus, STATUS_ENABLED));
    }

    /**
     * 获取默认排序值。
     *
     * @param sort 排序值
     * @return 最终排序值
     */
    private Integer defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    /**
     * 获取当前操作人。
     *
     * @return 操作人账号
     */
    private String currentOperator() {
        try {
            String username = getUsername();
            return StringUtils.hasText(username) ? username : DEFAULT_OPERATOR;
        } catch (RuntimeException ex) {
            return DEFAULT_OPERATOR;
        }
    }

    /**
     * 归一化可空文本。
     *
     * @param value 原始文本
     * @return 归一化后的文本
     */
    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 归一化可空文本并提供默认值。
     *
     * @param value        原始文本
     * @param defaultValue 默认值
     * @return 归一化后的文本
     */
    private String normalizeNullableText(String value, String defaultValue) {
        String normalized = normalizeNullableText(value);
        return normalized != null ? normalized : normalizeNullableText(defaultValue);
    }

    /**
     * 提供商解析结果。
     *
     * @param providerName 提供商名称
     * @param baseUrl      基础地址
     * @param description  描述
     */
    private record ProviderResolved(String providerName,
                                    String baseUrl,
                                    String description) {
    }
}
