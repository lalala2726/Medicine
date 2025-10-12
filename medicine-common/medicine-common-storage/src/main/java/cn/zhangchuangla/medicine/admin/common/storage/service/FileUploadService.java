package cn.zhangchuangla.medicine.admin.common.storage.service;

import cn.zhangchuangla.medicine.admin.model.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/25 09:49
 */
public interface FileUploadService {

    FileUploadVo upload(MultipartFile file);

}
