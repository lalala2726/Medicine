package cn.zhangchuangla.medicine.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 大模型提供商列表数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmProviderListDto {

    private Long id;

    private String providerName;

    private String baseUrl;

    private String description;

    private Integer status;

    private Integer sort;

    private String createBy;

    private String updateBy;

    private Date createdAt;

    private Date updatedAt;

    private Long modelCount;

    private Long chatModelCount;

    private Long rerankModelCount;

    private Long embeddingModelCount;
}
