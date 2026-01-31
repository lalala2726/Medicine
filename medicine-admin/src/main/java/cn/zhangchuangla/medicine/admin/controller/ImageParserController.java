package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.service.ImageParserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.MedicineInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/image_parser")
@Tag(name = "图片解析", description = "将图片信息解析对应的JSON")
public class ImageParserController extends BaseController {

    private final ImageParserService imageParserService;

    @PostMapping("/medicine_info")
    @Operation(summary = "解析药品图片信息", description = "将图片信息解析对应的JSON")
    public AjaxResult<MedicineInfoDto> medicineInfo() {
        MedicineInfoDto medicineInfoDto = imageParserService.parserMedicineInfo();
        return success(medicineInfoDto);
    }


}
