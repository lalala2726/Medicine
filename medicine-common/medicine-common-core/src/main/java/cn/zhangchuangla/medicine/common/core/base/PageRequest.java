package cn.zhangchuangla.medicine.common.core.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 */
@Data
public class PageRequest {

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", type = "integer", format = "int32", defaultValue = "1", requiredMode = Schema.RequiredMode.AUTO)
    private int pageNum = 1;

    /**
     * 每页数量
     */
    @Schema(description = "当前页码", type = "integer", format = "int32", defaultValue = "10", requiredMode = Schema.RequiredMode.AUTO)
    private int pageSize = 10;


    public <T> Page<T> toPage() {
        long current = Math.max(this.pageNum, 1);
        long size = Math.max(this.pageSize, 1);
        return new Page<>(current, size);
    }
}
