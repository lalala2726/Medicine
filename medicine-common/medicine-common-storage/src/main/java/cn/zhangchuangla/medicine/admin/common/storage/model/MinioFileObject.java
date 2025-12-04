package cn.zhangchuangla.medicine.admin.common.storage.model;

import lombok.Builder;
import lombok.Data;

/**
 * MinIO 文件实体封装
 * <p>
 * 便于在服务层统一携带桶名、对象路径、原始文件名、Content-Type 与二进制数据，
 * 复用下载后的内容而无需在各个业务里重复解析。
 */
@Data
@Builder
public class MinioFileObject {

    /**
     * 所属桶名
     */
    private String bucket;

    /**
     * 对象在桶中的完整路径
     */
    private String objectName;

    /**
     * 原始文件名（从对象路径提取）
     */
    private String filename;

    /**
     * MinIO 中存储的 Content-Type
     */
    private String contentType;

    /**
     * 文件二进制内容
     */
    private byte[] data;
}
