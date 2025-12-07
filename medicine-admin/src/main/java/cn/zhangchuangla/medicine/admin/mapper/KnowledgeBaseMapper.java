package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.dto.KnowledgeBaseStatsDto;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.model.entity.KnowledgeBase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    Page<KnowledgeBaseStatsDto> selectPageWithStats(Page<KnowledgeBaseStatsDto> page,
                                                    @Param("request") KnowledgeBaseListRequest request);
}




