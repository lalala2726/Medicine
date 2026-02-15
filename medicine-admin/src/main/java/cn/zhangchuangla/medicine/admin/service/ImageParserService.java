package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.dto.MedicineInfoDto;
import cn.zhangchuangla.medicine.model.request.ParserMedicineInfoRequest;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
@Deprecated(forRemoval = true)
public interface ImageParserService {

    /**
     * 解析药品信息
     *
     * @return 解析药品图片信息
     */
    MedicineInfoDto parserMedicineInfo(ParserMedicineInfoRequest request);
}
