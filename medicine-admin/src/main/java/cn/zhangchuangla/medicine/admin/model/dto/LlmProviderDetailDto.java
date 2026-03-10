package cn.zhangchuangla.medicine.admin.model.dto;

import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 大模型提供商详情数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmProviderDetailDto {

    private Long id;

    private String providerName;

    private String baseUrl;

    private String apiKey;

    private String description;

    private Integer status;

    private Integer sort;

    private String createBy;

    private String updateBy;

    private Date createdAt;

    private Date updatedAt;

    private List<LlmProviderModel> models;
}
