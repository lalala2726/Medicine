package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.AssistantService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
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
        if (imageUrls == null || imageUrls.isEmpty()) { // 无图片输入直接报参数错误
            throw new ServiceException(ResponseCode.PARAM_ERROR, "图片地址不能为空");
        }

        List<String> base64Images = new ArrayList<>(imageUrls.size()); // 按数量预分配返回列表
        for (String imageUrl : imageUrls) { // 逐个处理传入的 URL
            if (imageUrl == null || imageUrl.isBlank()) { // 单个 URL 为空时提示参数错误
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片地址不能为空");
            }

            MinioLocation location = parseMinioLocation(imageUrl);
            try (InputStream objectStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(location.bucket())
                    .object(location.object())
                    .build())) {
                // 一次性读取对象内容
                byte[] fileBytes = objectStream.readAllBytes();
                String mimeType = guessMimeType(location.object());
                String base64WithPrefix = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(fileBytes);
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
        try { // 使用 URI 解析保证地址合法
            URI uri = new URI(imageUrl); // 构造 URI 对象
            String path = uri.getPath(); // 仅取路径部分
            if (path == null || path.isBlank()) { // 路径缺失视为格式错误
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片路径格式不正确");
            }

            String normalizedPath = path.startsWith("/") ? path.substring(1) : path; // 去掉前导斜杠
            int firstSlash = normalizedPath.indexOf('/'); // 找到 bucket 与 object 的分隔符
            if (firstSlash <= 0 || firstSlash == normalizedPath.length() - 1) { // 分隔符位置非法
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片路径格式不正确");
            }

            String bucket = normalizedPath.substring(0, firstSlash); // 提取 bucket 名
            String object = normalizedPath.substring(firstSlash + 1); // 提取对象路径
            if (bucket.isBlank() || object.isBlank()) { // 任意为空则格式错误
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片路径格式不正确");
            }

            return new MinioLocation(bucket, object); // 返回解析结果
        } catch (URISyntaxException ex) { // URI 解析失败直接提示地址不合法
            throw new ServiceException(ResponseCode.PARAM_ERROR, "图片地址格式不正确");
        }
    }

    private record MinioLocation(String bucket, String object) {
    }

}
