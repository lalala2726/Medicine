package cn.zhangchuangla.medicine.config;

import cn.zhangchuangla.medicine.enums.FileStorageMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.zhangchuangla.medicine.constants.Constants.STATIC_FILE;

/**
 * 文件上传相关配置属性。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /**
     * 允许的文件类型，逗号分隔。
     */
    private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp,application/pdf";

    /**
     * 文件存储模式（local|minio）。
     */
    private FileStorageMode mode = FileStorageMode.MINIO;

    /**
     * 文件域名，用于拼接访问地址。
     */
    private String domain;

    /**
     * 本地文件存储的根路径。
     */
    private String localPath = Paths.get(System.getProperty("java.io.tmpdir"), "medicine", "upload").toString();

    /**
     * 获取允许的文件类型集合。
     *
     * @return 允许的类型集合
     */
    public Set<String> getAllowedTypeSet() {
        if (allowedTypes == null || allowedTypes.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 构建文件访问地址。
     *
     * @param relativePath 文件相对路径
     * @return 完整访问地址
     */
    public String buildFileUrl(String relativePath) {
        String normalizedDomain = normalizeDomain(domain);
        String normalizedAccessPath = normalizeAccessPath(STATIC_FILE);
        String normalizedRelativePath = normalizeRelativePath(relativePath);
        return normalizedDomain + normalizedAccessPath + normalizedRelativePath;
    }

    /**
     * 获取本地存储根路径。
     *
     * @return 根路径
     */
    public Path getLocalBasePath() {
        if (localPath == null || localPath.isBlank()) {
            throw new IllegalStateException("file.upload.local-path 未配置");
        }
        return Paths.get(localPath).toAbsolutePath().normalize();
    }

    /**
     * 获取资源处理器匹配路径，如 /files/**
     *
     * @return 资源匹配路径
     */
    public String getAccessPathPattern() {
        String normalizedAccessPath = normalizeAccessPath(STATIC_FILE);
        if (normalizedAccessPath.isEmpty()) {
            return "/**";
        }
        return normalizedAccessPath + "/**";
    }

    private String normalizeDomain(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("file.upload.domain 未配置");
        }
        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private String normalizeAccessPath(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        return trimmed.endsWith("/") && trimmed.length() > 1 ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private String normalizeRelativePath(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("relativePath 不能为空");
        }
        String replaced = value.replace("\\", "/");
        String trimmed = replaced.startsWith("/") ? replaced.substring(1) : replaced;
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }
}
