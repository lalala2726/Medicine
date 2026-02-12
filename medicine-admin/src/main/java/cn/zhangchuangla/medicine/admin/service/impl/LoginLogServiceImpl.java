package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.LoginLogMapper;
import cn.zhangchuangla.medicine.admin.model.request.LoginLogQueryRequest;
import cn.zhangchuangla.medicine.admin.service.LoginLogService;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.SysLoginLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 系统登录日志服务实现。
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, SysLoginLog>
        implements LoginLogService {

    @Override
    public Page<SysLoginLog> logList(LoginLogQueryRequest query) {
        Page<SysLoginLog> page = query.toPage();
        return baseMapper.logList(page, query);
    }

    @Override
    public SysLoginLog getLogById(Long id) {
        Assert.isPositive(id, "日志ID必须大于0");
        return getById(id);
    }

    @Override
    public boolean clearLogs() {
        baseMapper.delete(new LambdaQueryWrapper<SysLoginLog>().isNotNull(SysLoginLog::getId));
        return true;
    }
}
