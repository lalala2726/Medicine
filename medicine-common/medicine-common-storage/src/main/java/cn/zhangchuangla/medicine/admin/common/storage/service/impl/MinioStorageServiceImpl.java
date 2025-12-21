package cn.zhangchuangla.medicine.admin.common.storage.service.impl;

import cn.zhangchuangla.medicine.admin.common.storage.config.MinioConfig;
import cn.zhangchuangla.medicine.admin.common.storage.model.MinioFileObject;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import io.minio.*;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * MinIO存储服务实现类
 *
 * @author Chuang
 * created on 2025/9/25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements MinioStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public void checkBucketExists(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to check/create bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to check/create bucket: " + bucketName, e);
        }
    }

    @Override
    public String uploadFile(String bucketName, String objectName, InputStream inputStream, long contentLength, String contentType) {
        try {
            checkBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, contentLength, -1)
                            .contentType(contentType)
                            .build()
            );

            return getFileUrl(bucketName, objectName);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", objectName, e);
            throw new RuntimeException("Failed to upload file: " + objectName, e);
        }
    }

    @Override
    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            checkBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return getFileUrl(bucketName, objectName);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", objectName, e);
            throw new RuntimeException("Failed to upload file: " + objectName, e);
        }
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectName, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        try {
            // 构建直接的文件访问URL，不使用预签名URL
            return String.format("%s/%s/%s", normalizeEndpoint(minioConfig.getEndpoint()), bucketName, objectName);
        } catch (Exception e) {
            log.error("Failed to get file URL: {}", objectName, e);
            throw new RuntimeException("Failed to get file URL: " + objectName, e);
        }
    }

    @Override
    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("Failed to list buckets", e);
            throw new RuntimeException("Failed to list buckets", e);
        }
    }

    @Override
    public boolean fileExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public MinioFileObject fetchFileByUrl(String fileUrl) {
        // 统一从 MinIO URL 解析 bucket/object，再读取文件内容，供业务复用
        MinioLocation location = parseLocation(fileUrl);
        try {
            // 先读取对象属性，拿到 Content-Type 便于后续业务做类型判断
            StatObjectResponse statObject = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.object())
                            .build()
            );
            try (InputStream objectStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.object())
                            .build())) {
                byte[] data = objectStream.readAllBytes();
                return MinioFileObject.builder()
                        .bucket(location.bucket())
                        .objectName(location.object())
                        .filename(extractFilename(location.object()))
                        .contentType(statObject != null ? statObject.contentType() : null)
                        .data(data)
                        .build();
            }
        } catch (Exception ex) {
            log.error("Failed to fetch file from MinIO, url: {}", fileUrl, ex);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "读取文件失败，请稍后重试");
        }
    }

    @Contract("null -> fail")
    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("minio.endpoint 未配置");
        }
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
    }

    /**
     * 将完整的 MinIO URL 拆解为 bucket 与 object，方便后续下载。
     */
    private MinioLocation parseLocation(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "文件路径格式不正确");
            }

            String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
            int firstSlash = normalizedPath.indexOf('/');
            if (firstSlash <= 0 || firstSlash == normalizedPath.length() - 1) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "文件路径格式不正确");
            }

            String bucket = normalizedPath.substring(0, firstSlash);
            String object = normalizedPath.substring(firstSlash + 1);
            if (!StringUtils.hasText(bucket) || !StringUtils.hasText(object)) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "文件路径格式不正确");
            }
            return new MinioLocation(bucket, object);
        } catch (URISyntaxException ex) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "文件地址格式不正确");
        }
    }

    private String extractFilename(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return "unknown";
        }
        int lastSlash = objectName.lastIndexOf('/');
        return lastSlash >= 0 ? objectName.substring(lastSlash + 1) : objectName;
    }

    private record MinioLocation(String bucket, String object) {
    }

}
