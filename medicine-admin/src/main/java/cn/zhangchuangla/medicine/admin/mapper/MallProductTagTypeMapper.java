package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.model.entity.MallProductTagType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 商品标签类型 Mapper。
 *
 * @author Chuang
 */
public interface MallProductTagTypeMapper extends BaseMapper<MallProductTagType> {

    /**
     * 物理删除标签类型。
     *
     * @param id 标签类型ID
     * @return 影响行数
     */
    @Delete("DELETE FROM mall_product_tag_type WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
