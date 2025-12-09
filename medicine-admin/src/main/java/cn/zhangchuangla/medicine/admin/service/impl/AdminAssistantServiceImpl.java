package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.common.storage.model.MinioFileObject;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.admin.service.AdminAssistantService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.ImageUtils;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import cn.zhangchuangla.medicine.llm.model.request.AssistantChatRequest;
import cn.zhangchuangla.medicine.llm.service.AssistantService;
import cn.zhangchuangla.medicine.llm.service.LLMParseImageService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
            ImageUtils.EncodedImage encoded = ImageUtils.ensureUnderForText(fileObject.getData(), originalMimeType);
            String base64WithPrefix = "data:" + encoded.mimeType() + ";base64," + Base64.getEncoder().encodeToString(encoded.data());
            base64Images.add(base64WithPrefix);
        }
        // 将带 data URI 前缀的 Base64 列表传给大模型
        return llmParseImageService.parseImage(base64Images);
    }

    @Override
    public SseEmitter chat(AssistantChatRequest request) {
        if (request == null || (!StringUtils.hasText(request.getMessage())
                && (request.getFileUrls() == null || request.getFileUrls().isEmpty()))) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "消息或文件至少提供一项");
        }
        String enriched = buildMessageWithFiles(request.getMessage(), request.getFileUrls());
        return assistantService.AdminAssistantChat(enriched);
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

    /**
     * 将文件解析内容追加到消息末尾。
     */
    private String buildMessageWithFiles(String baseMessage, List<String> fileUrls) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(baseMessage)) {
            builder.append(baseMessage.trim());
        }
        if (fileUrls == null || fileUrls.isEmpty()) {
            return builder.toString();
        }
        List<String> texts = fileUrls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(this::parseFileContent)
                .filter(StringUtils::hasText)
                .toList();
        if (!texts.isEmpty()) {
            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append("以下是用户上传文件的内容摘要：\n");
            for (int i = 0; i < texts.size(); i++) {
                builder.append("【文件").append(i + 1).append("】\n")
                        .append(texts.get(i)).append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * 使用 Spring AI TikaDocumentReader 解析文件；图片留待后续扩展。
     */
    private String parseFileContent(String fileUrl) {
        MinioFileObject fileObject = minioStorageService.fetchFileByUrl(fileUrl);
        if (isImage(fileObject)) {
            return "[图片文件，暂未解析：" + fileObject.getFilename() + "]";
        }
        ByteArrayResource resource = new ByteArrayResource(fileObject.getData()) {
            @Override
            public String getFilename() {
                return fileObject.getFilename();
            }

            @NotNull
            @Override
            public String getDescription() {
                return fileObject.getObjectName();
            }
        };
        List<Document> documents = new TikaDocumentReader(resource).get();
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        return documents.stream()
                .map(Document::getText)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }

    private boolean isImage(MinioFileObject fileObject) {
        String name = fileObject.getFilename();
        String contentType = fileObject.getContentType();
        String lowerName = name == null ? "" : name.toLowerCase();
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
            return true;
        }
        return lowerName.endsWith(".png") || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp");
    }

}
