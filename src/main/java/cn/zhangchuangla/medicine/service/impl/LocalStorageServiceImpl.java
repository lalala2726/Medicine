package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.config.FileUploadProperties;
import cn.zhangchuangla.medicine.enums.FileStorageMode;
import cn.zhangchuangla.medicine.service.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements LocalStorageService {

    private final FileUploadProperties fileUploadProperties;

    @Override
    public FileStorageMode getMode() {
        return FileStorageMode.LOCAL;
    }

    @Override
    public String upload(String relativePath, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        Path targetPath = resolveTargetPath(relativePath);

        try (InputStream inputStream = file.getInputStream()) {
            Path parent = targetPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored file locally at {}", targetPath);
            return fileUploadProperties.buildFileUrl(relativePath);
        } catch (IOException e) {
            log.error("Failed to store local file: {}", relativePath, e);
            throw new ServiceException("本地文件保存失败");
        }
    }

    private Path resolveTargetPath(String relativePath) {
        Path basePath = fileUploadProperties.getLocalBasePath();
        Path targetPath = basePath.resolve(relativePath).normalize();
        if (!targetPath.startsWith(basePath)) {
            throw new IllegalArgumentException("非法的文件路径");
        }
        return targetPath;
    }
}
