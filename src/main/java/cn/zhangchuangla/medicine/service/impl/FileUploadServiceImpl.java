package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.model.vo.FileUploadVo;
import cn.zhangchuangla.medicine.service.FileUploadService;
import cn.zhangchuangla.medicine.service.MinioStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务实现类
 *
 * @author Chuang
 * created on 2025/9/25 09:49
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final MinioStorageService minioStorageService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}")
    private String allowedTypes;

    private Set<String> allowedTypeSet;

    @PostConstruct
    public void init() {
        // 初始化允许的文件类型集合
        if (allowedTypes != null && !allowedTypes.trim().isEmpty()) {
            allowedTypeSet = Set.of(allowedTypes.split(","));
        } else {
            allowedTypeSet = Collections.emptySet();
        }
    }

    @Override
    public FileUploadVo upload(MultipartFile file) {
        try {
            // 参数校验
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            // 获取文件原始名称
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("文件名不能为空");
            }
            // 文件类型校验
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedFileType(contentType)) {
                throw new IllegalArgumentException("不支持的文件类型: " + contentType);
            }

            // 生成年月文件夹路径
            String folderPath = generateYearMonthFolderPath();

            // 生成唯一文件名
            String uniqueFileName = generateUniqueFileName(originalFilename);

            // 构建完整的对象路径
            String objectName = folderPath + "/" + uniqueFileName;

            // 上传文件到MinIO
            String fileUrl = minioStorageService.uploadFile(bucketName, objectName, file.getInputStream(), file.getSize(), file.getContentType());

            // 构建返回结果
            FileUploadVo uploadVo = new FileUploadVo();
            uploadVo.setFileName(originalFilename);
            uploadVo.setFileSize(file.getSize());
            uploadVo.setFileType(file.getContentType());
            uploadVo.setFileUrl(fileUrl);

            log.info("File uploaded successfully: {}", originalFilename);
            return uploadVo;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成年月文件夹路径
     * 格式：yyyy/MM
     *
     * @return 文件夹路径
     */
    private String generateYearMonthFolderPath() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
        return now.format(formatter);
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + fileExtension;
    }

    /**
     * 检查文件类型是否允许
     *
     * @param contentType 文件类型
     * @return 是否允许
     */
    private boolean isAllowedFileType(String contentType) {
        return allowedTypeSet.contains(contentType);
    }
}
