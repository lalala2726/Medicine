package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
public interface AssistantService {


    DrugInfoDto parseDrugInfoByImage(List<String> imageUrls);
}
