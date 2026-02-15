package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.ImageParserService;
import cn.zhangchuangla.medicine.agent.service.AgentImageParseService;
import cn.zhangchuangla.medicine.model.dto.MedicineInfoDto;
import cn.zhangchuangla.medicine.model.request.ParserMedicineInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
@Service
@RequiredArgsConstructor
@Deprecated(forRemoval = true)
public class ImageParserServiceImpl implements ImageParserService {

    private final AgentImageParseService agentImageParseService;


    @Override
    public MedicineInfoDto parserMedicineInfo(ParserMedicineInfoRequest request) {
        return agentImageParseService.medicineInfoParse(request.getImageUrls());
    }
}
