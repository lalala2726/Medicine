package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
@Mapper
public interface KbBaseMapper extends BaseMapper<KbBase> {

    /**
     * 知识库分页列表（XML 组装查询条件，提升 SQL 可读性）。
     *
     * @param page    分页对象
     * @param request 查询参数
     * @return 知识库分页
     */
    Page<KbBase> listKnowledgeBase(Page<KbBase> page, @Param("request") KnowledgeBaseListRequest request);
}


