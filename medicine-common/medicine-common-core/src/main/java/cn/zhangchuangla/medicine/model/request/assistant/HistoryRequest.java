package cn.zhangchuangla.medicine.model.request.assistant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/18 09:06
 */
@Data
@Schema(description = "历史消息分页请求")
public class HistoryRequest {

    /**
     * 会话UUID
     */
    @Schema(description = "会话UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "90dda9d7-bbd2-448d-ab4d-62a91a708088")
    private String uuid;

    /**
     * 游标ID（用于分页，传入上一页最后一条消息的ID）
     */
    @Schema(description = "游标ID（用于分页，传入上一页最后一条消息的ID）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "123")
    private Long cursor;

    /**
     * 每页条数，默认20
     */
    @Schema(description = "每页条数，默认20", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "20")
    private Integer limit = 20;

}
