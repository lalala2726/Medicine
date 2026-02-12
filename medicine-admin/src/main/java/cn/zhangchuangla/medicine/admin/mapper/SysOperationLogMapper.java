package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.OperationLogQueryRequest;
import cn.zhangchuangla.medicine.model.entity.SysOperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * 系统操作日志 Mapper。
 */
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    /**
     * 分页查询操作日志列表。
     */
    Page<SysOperationLog> logList(@Param("page") Page<SysOperationLog> page,
                                  @Param("query") OperationLogQueryRequest query);
}
