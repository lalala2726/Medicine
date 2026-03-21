package cn.zhangchuangla.medicine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品标签筛选分组对象。
 *
 * @author Chuang
 */
@Data
@Schema(description = "商品标签筛选分组对象")
public class MallProductTagFilterGroup {

    /**
     * 标签类型ID。
     */
    @Schema(description = "标签类型ID", example = "1")
    private Long typeId;

    /**
     * 标签类型编码。
     */
    @Schema(description = "标签类型编码", example = "CROWD")
    private String typeCode;

    /**
     * 当前类型下的标签ID列表。
     */
    @Schema(description = "当前类型下的标签ID列表", example = "[1,2]")
    private List<Long> tagIds;
}
