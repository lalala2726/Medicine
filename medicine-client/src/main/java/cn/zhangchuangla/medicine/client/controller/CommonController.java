package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.admin.common.storage.service.FileUploadService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.FileUploadVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/20
 */
@RestController
@RequestMapping("/common")
@Slf4j
@Tag(name = "通用接口")
public class CommonController extends BaseController {

    private final FileUploadService fileUploadService;

    public CommonController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }


    /**
     * 文件上传接口
     *
     * @param file 文件
     * @return 文件上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public AjaxResult<FileUploadVo> upload(MultipartFile file) {
        FileUploadVo upload = fileUploadService.upload(file);
        return success(upload);
    }
}
