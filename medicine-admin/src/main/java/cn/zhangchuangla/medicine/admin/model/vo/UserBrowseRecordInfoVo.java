package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/7 04:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户浏览记录信息")
public class UserBrowseRecordInfoVo {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "1")
    private Long productId;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "商品名称")
    private String productName;

    /**
     * 浏览时间
     */
    @Schema(description = "浏览时间", example = "2025-11-06 16:46:00")
    private Date browseTime;
}
