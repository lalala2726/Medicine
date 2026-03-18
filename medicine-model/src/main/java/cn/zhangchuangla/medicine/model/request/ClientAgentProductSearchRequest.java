package cn.zhangchuangla.medicine.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户端智能体商品搜索请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户端智能体商品搜索请求")
public class ClientAgentProductSearchRequest extends PageRequest {

    /**
     * 商品搜索关键词。
     */
    @Schema(description = "搜索关键词", example = "阿莫西林")
    private String keyword;

    /**
     * 商品分类名称。
     */
    @Schema(description = "商品分类名称", example = "感冒药")
    private String categoryName;

    /**
     * 商品用途或适用场景。
     */
    @Schema(description = "商品用途或适用场景", example = "缓解感冒引起的头痛、鼻塞")
    private String usage;

    /**
     * 校验搜索关键词、分类名称、用途至少提供一项。
     *
     * @return 是否提供了有效搜索条件
     */
    @AssertTrue(message = "搜索关键词、分类名称、用途不能同时为空")
    @Schema(hidden = true)
    public boolean isSearchConditionPresent() {
        return hasText(keyword) || hasText(categoryName) || hasText(usage);
    }

    /**
     * 每页条数，客户端智能体场景限制最大 20 条。
     *
     * @return 每页条数
     */
    @Override
    @Max(value = 20, message = "每页数量不能超过20")
    @Schema(description = "每页数量", type = "integer", format = "int32", defaultValue = "10")
    public int getPageSize() {
        return super.getPageSize();
    }

    /**
     * 判断字符串是否包含有效文本。
     *
     * @param value 待判断字符串
     * @return 是否包含有效文本
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
