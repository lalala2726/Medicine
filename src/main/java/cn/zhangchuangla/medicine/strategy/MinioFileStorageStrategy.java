package cn.zhangchuangla.medicine.strategy;

import cn.zhangchuangla.medicine.enums.FileStorageMode;
import cn.zhangchuangla.medicine.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 基于 MinIO 的文件存储策略。
 */
@Component
@RequiredArgsConstructor
public class MinioFileStorageStrategy implements FileStorageStrategy {

    private final MinioStorageService minioStorageService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public FileStorageMode getMode() {
        return FileStorageMode.MINIO;
    }

    @Override
    public String upload(String relativePath, MultipartFile file) {
        return minioStorageService.uploadFile(bucketName, relativePath, file);
    }
}
