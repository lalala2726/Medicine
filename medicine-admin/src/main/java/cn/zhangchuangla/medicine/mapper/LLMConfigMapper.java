package cn.zhangchuangla.medicine.mapper;

import cn.zhangchuangla.medicine.model.entity.LlmConfig;
import cn.zhangchuangla.medicine.model.request.llm.LlmConfigListQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * LLM配置Mapper接口
 *
 * @author Chuang
 */
@Mapper
public interface LLMConfigMapper extends BaseMapper<LlmConfig> {

    /**
     * LLM配置列表
     *
     * @param llmConfigPage 分页对象
     * @param request       LLM配置列表查询请求参数
     * @return LLM配置列表
     */
    Page<LlmConfig> listLlmConfig(Page<LlmConfig> llmConfigPage, @Param("request") LlmConfigListQueryRequest request);

}




