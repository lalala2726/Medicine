package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.model.entity.MallProductTag;
import cn.zhangchuangla.medicine.model.vo.MallProductTagVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品标签服务（客户端）。
 *
 * @author Chuang
 */
public interface MallProductTagService extends IService<MallProductTag> {

    /**
     * 为搜索请求补充按类型分组后的标签筛选条件。
     *
     * @param request 搜索请求
     */
    void fillSearchTagGroups(MallProductSearchRequest request);

    /**
     * 查询商品ID与启用标签列表的映射。
     *
     * @param productIds 商品ID列表
     * @return 商品ID到标签列表的映射
     */
    Map<Long, List<MallProductTagVo>> listEnabledTagVoMapByProductIds(List<Long> productIds);
}
