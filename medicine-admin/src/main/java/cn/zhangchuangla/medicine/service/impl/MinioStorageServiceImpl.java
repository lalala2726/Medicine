package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.service.MinioStorageService;
import io.minio.*;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * MinIO存储服务实现类
 *
 * @author Chuang
 * created on 2025/9/25 09:57
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements MinioStorageService {

    private final MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String bucketName;
    @Value("${minio.endpoint}")
    private String endpoint;

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
            return String.format("%s/%s/%s", endpoint, bucketName, objectName);
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

}
