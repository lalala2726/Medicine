package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.admin.common.storage.service.FileUploadService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.FileUploadVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口（管理端）
 *
 * @author Chuang
 * created on 2025/9/25
 */
@RestController
@RequestMapping("/file")
@Tag(name = "文件上传接口", description = "文件上传接口")
public class FileUploadController extends BaseController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
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
