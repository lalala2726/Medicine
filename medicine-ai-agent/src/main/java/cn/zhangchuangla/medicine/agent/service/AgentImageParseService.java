package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.model.dto.MedicineInfoDto;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2026/2/1
 */
public interface AgentImageParseService {


    /**
     * 解析药品信息
     *
     * @param imageUrls 图片URL列表
     * @return 药品信息
     */
    MedicineInfoDto medicineInfoParse(List<String> imageUrls);
}
