package cn.zhangchuangla.agent.service.impl;

import cn.zhangchuangla.agent.config.AgentProperties;
import cn.zhangchuangla.agent.constanst.URLConstant;
import cn.zhangchuangla.agent.service.AgentImageParseService;
import cn.zhangchuangla.medicine.admin.common.storage.model.MinioFileObject;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.ImageUtils;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@Service
@RequiredArgsConstructor
public class AgentImageParseServiceImpl implements AgentImageParseService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final MinioStorageService minioStorageService;
    private final OkHttpClient okHttpClient;
    private final AgentProperties agentProperties;

    @Override
    public DrugInfoDto parseDrugInfoByImage(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "图片地址不能为空");
        }

        List<String> base64Images = new ArrayList<>(imageUrls.size());
        for (String imageUrl : imageUrls) {
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片地址不能为空");
            }

            MinioFileObject fileObject = minioStorageService.fetchFileByUrl(imageUrl);
            String originalMimeType = StringUtils.hasText(fileObject.getContentType())
                    ? fileObject.getContentType()
                    : guessMimeType(fileObject.getFilename());
            ImageUtils.EncodedImage encoded = ImageUtils.ensureUnderForText(fileObject.getData(), originalMimeType);
            String base64Image = Base64.getEncoder().encodeToString(encoded.data());
            base64Images.add(base64Image);
        }
        return parseDrugInfoByFastApi(base64Images);
    }

    private DrugInfoDto parseDrugInfoByFastApi(List<String> base64Images) {
        String payload = JSON.toJSONString(Map.of("images", base64Images));
        String requestUrl = buildRequestUrl();
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析失败");
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析失败");
            }
            String responseText = responseBody.string();
            if (!StringUtils.hasText(responseText)) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析结果为空");
            }
            return parseDrugInfoResponse(responseText);
        } catch (IOException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析服务不可用");
        }
    }

    private DrugInfoDto parseDrugInfoResponse(String responseText) {
        try {
            JSONObject jsonObject = JSON.parseObject(responseText);
            if (jsonObject == null) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析结果为空");
            }
            if (jsonObject.containsKey("data")) {
                return jsonObject.getObject("data", DrugInfoDto.class);
            }
            return jsonObject.to(DrugInfoDto.class);
        } catch (Exception ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析响应解析失败");
        }
    }

    private String guessMimeType(String objectName) {
        String lower = objectName == null ? "" : objectName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String buildRequestUrl() {
        String baseUrl = agentProperties.getUrl();
        if (!StringUtils.hasText(baseUrl)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析服务地址未配置");
        }
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return normalizedBaseUrl + URLConstant.DRUG_IMAGE_PARSE;
    }
}
