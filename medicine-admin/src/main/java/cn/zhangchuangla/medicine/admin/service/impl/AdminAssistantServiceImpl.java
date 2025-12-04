package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.common.storage.model.MinioFileObject;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.admin.service.AdminAssistantService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.ImageUtils;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import cn.zhangchuangla.medicine.llm.service.AssistantService;
import cn.zhangchuangla.medicine.llm.service.LLMParseImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@Service
@RequiredArgsConstructor
public class AdminAssistantServiceImpl implements AdminAssistantService {

    private final MinioStorageService minioStorageService;
    private final LLMParseImageService llmParseImageService;
    private final AssistantService assistantService;


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

            // 复用公共存储工具从 MinIO 拉取文件，避免各处重复解析 URL
            MinioFileObject fileObject = minioStorageService.fetchFileByUrl(imageUrl);
            String originalMimeType = StringUtils.hasText(fileObject.getContentType())
                    ? fileObject.getContentType()
                    : guessMimeType(fileObject.getFilename());
            ImageUtils.EncodedImage encoded = ImageUtils.ensureUnder1MB(fileObject.getData(), originalMimeType);
            String base64WithPrefix = "data:" + encoded.mimeType() + ";base64," + Base64.getEncoder().encodeToString(encoded.data());
            base64Images.add(base64WithPrefix);
        }
        // 将带 data URI 前缀的 Base64 列表传给大模型
        return llmParseImageService.parseImage(base64Images);
    }

    @Override
    public SseEmitter chat(String message) {
        return assistantService.chat(message);
    }

    private String guessMimeType(String objectName) {
        String lower = objectName == null ? "" : objectName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".jpeg") || lower.endsWith(".jpg")) {
            return "image/jpeg";
        }
        return "image/jpeg";
    }

}
