package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.LoginLogQueryRequest;
import cn.zhangchuangla.medicine.model.entity.SysLoginLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统登录日志服务。
 */
public interface LoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志列表。
     */
    Page<SysLoginLog> logList(LoginLogQueryRequest query);

    /**
     * 根据日志ID查询详情。
     */
    SysLoginLog getLogById(Long id);

    /**
     * 清空登录日志。
     */
    boolean clearLogs();
}
