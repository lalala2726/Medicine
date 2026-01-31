package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.model.dto.DrugInfoDto;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
public interface AgentImageParseService {

    /**
     * Parse drug information from images.
     *
     * @param imageUrls image URLs
     * @return drug information
     */
    DrugInfoDto parseDrugInfoByImage(List<String> imageUrls);
}
