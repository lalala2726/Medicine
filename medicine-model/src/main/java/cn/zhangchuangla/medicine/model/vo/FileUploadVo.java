package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/25
 */
@Data
@Schema(description = "文件上传返回参数")
public class FileUploadVo {

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件访问地址")
    private String fileUrl;

}
