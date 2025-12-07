package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface KbDocumentChunkMapper extends BaseMapper<KbDocumentChunk> {

    Page<KbDocumentChunk> selectDocumentSlices(Page<KbDocumentChunk> page,
                                               @Param("docId") Long docId,
                                               @Param("name") String name);
}




