package cn.zhangchuangla.medicine.common.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文件上传相关配置属性，仅负责基础校验配置。
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
}
