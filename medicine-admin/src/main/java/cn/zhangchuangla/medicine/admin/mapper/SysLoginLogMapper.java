package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.LoginLogQueryRequest;
import cn.zhangchuangla.medicine.model.entity.SysLoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * 系统登录日志 Mapper。
 */
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 分页查询登录日志列表。
     */
    Page<SysLoginLog> logList(@Param("page") Page<SysLoginLog> page,
                              @Param("query") LoginLogQueryRequest query);
}
