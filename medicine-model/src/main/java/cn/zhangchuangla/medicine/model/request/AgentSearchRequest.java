package cn.zhangchuangla.medicine.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Chuang
 * <p>
 * created on 2026/2/2
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentSearchRequest extends PageRequest {

    @Schema(description = "关键字", example = "张三")
    private String keyword;
}
