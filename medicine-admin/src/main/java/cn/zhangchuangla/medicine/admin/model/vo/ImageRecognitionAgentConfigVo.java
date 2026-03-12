package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图片识别 Agent 配置视图对象。
 */
@Data
@Schema(description = "图片识别Agent配置视图对象")
public class ImageRecognitionAgentConfigVo {

    @Schema(description = "图片识别模型配置")
    private AgentModelSelectionVo imageRecognitionModel;
}
