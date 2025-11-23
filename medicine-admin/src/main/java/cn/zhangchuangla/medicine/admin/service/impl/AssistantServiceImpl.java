package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.AssistantService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.ImageUtils;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import cn.zhangchuangla.medicine.llm.service.LLMParseImageService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@Service
public class AssistantServiceImpl implements AssistantService {

    private final MinioClient minioClient;
    private final LLMParseImageService llmParseImageService;

    public AssistantServiceImpl(MinioClient minioClient, LLMParseImageService llmParseImageService) {
        this.minioClient = minioClient;
        this.llmParseImageService = llmParseImageService;
    }


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

            MinioLocation location = parseMinioLocation(imageUrl);
            try (InputStream objectStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(location.bucket())
                    .object(location.object())
                    .build())) {
                // 一次性读取对象内容
                byte[] fileBytes = objectStream.readAllBytes();
                String originalMimeType = guessMimeType(location.object());
                ImageUtils.EncodedImage encoded = ImageUtils.ensureUnder1MB(fileBytes, originalMimeType);
                String base64WithPrefix = "data:" + encoded.mimeType() + ";base64," + Base64.getEncoder().encodeToString(encoded.data());
                base64Images.add(base64WithPrefix);
            } catch (Exception ex) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "读取图片失败，请稍后重试");
            }
        }
        // 将带 data URI 前缀的 Base64 列表传给大模型
        return llmParseImageService.parseImage(base64Images);
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


    private MinioLocation parseMinioLocation(String imageUrl) {
        try {
            // 构造 URI 对象
            URI uri = new URI(imageUrl);
            // 仅取路径部分
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片路径格式不正确");
            }
            // 去掉前导斜杠
            String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
            // 找到 bucket 与 object 的分隔符
            int firstSlash = normalizedPath.indexOf('/');
            // 分隔符位置非法
            if (firstSlash <= 0 || firstSlash == normalizedPath.length() - 1) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片路径格式不正确");
            }

            // 提取 bucket 名
            String bucket = normalizedPath.substring(0, firstSlash);
            // 提取对象路径
            String object = normalizedPath.substring(firstSlash + 1);
            // 任意为空则格式错误
            if (bucket.isBlank() || object.isBlank()) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片路径格式不正确");
            }
            // 返回解析结果
            return new MinioLocation(bucket, object);
        } catch (URISyntaxException ex) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "图片地址格式不正确");
        }
    }

    private record MinioLocation(String bucket, String object) {
    }

}
