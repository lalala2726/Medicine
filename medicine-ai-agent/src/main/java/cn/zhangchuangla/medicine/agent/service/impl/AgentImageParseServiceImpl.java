package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.admin.common.storage.model.MinioFileObject;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.agent.config.AgentProperties;
import cn.zhangchuangla.medicine.agent.constanst.URLConstant;
import cn.zhangchuangla.medicine.agent.service.AgentImageParseService;
import cn.zhangchuangla.medicine.agent.util.RequestClient;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.ImageUtils;
import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import cn.zhangchuangla.medicine.model.dto.DrugInfoDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    private final MinioStorageService minioStorageService;
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
        String payload = JSONUtils.toJson(Map.of("images", base64Images));
        String responseText = RequestClient.post(buildRequestUrl(), payload);
        if (!StringUtils.hasText(responseText)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片解析结果为空");
        }
        return parseDrugInfoResponse(responseText);
    }

    private DrugInfoDto parseDrugInfoResponse(String responseText) {
        try {
            JsonElement element = JSONUtils.parse(responseText);
            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    return JSONUtils.fromJson(jsonObject.get("data"), DrugInfoDto.class);
                }
            }
            return JSONUtils.fromJson(responseText, DrugInfoDto.class);
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
