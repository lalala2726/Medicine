package cn.zhangchuangla.medicine.strategy;

import cn.zhangchuangla.medicine.enums.FileStorageMode;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略。
 */
public interface FileStorageStrategy {

    /**
     * 返回策略支持的存储模式。
     *
     * @return 存储模式
     */
    FileStorageMode getMode();

    /**
     * 按照约定的相对路径上传文件，返回可访问的 URL。
     *
     * @param relativePath 相对路径（例如 yyyy/MM/uuid.ext）
     * @param file         上传文件
     * @return 文件访问 URL
     */
    String upload(String relativePath, MultipartFile file);
}
