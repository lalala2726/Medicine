package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.model.entity.DrugDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品详情 Mapper 接口。
 * <p>
 * 提供药品详情数据的数据访问操作，用于查询药品说明书等信息。
 *
 * @author Chuang
 */
@Mapper
public interface MallMedicineDetailMapper extends BaseMapper<DrugDetail> {
}
