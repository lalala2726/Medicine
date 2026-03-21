package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.model.entity.MallProductTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 商品标签 Mapper。
 *
 * @author Chuang
 */
public interface MallProductTagMapper extends BaseMapper<MallProductTag> {

    /**
     * 物理删除未绑定的标签。
     *
     * @param id 标签ID
     * @return 影响行数
     */
    @Delete("DELETE FROM mall_product_tag WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
