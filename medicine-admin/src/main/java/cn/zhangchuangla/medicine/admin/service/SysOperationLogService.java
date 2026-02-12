package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.OperationLogQueryRequest;
import cn.zhangchuangla.medicine.model.entity.SysOperationLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统操作日志服务。
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志列表。
     */
    Page<SysOperationLog> logList(OperationLogQueryRequest query);

    /**
     * 根据日志ID查询详情。
     */
    SysOperationLog getLogById(Long id);

    /**
     * 清空操作日志。
     */
    boolean clearLogs();
}
