package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.SysOperationLogMapper;
import cn.zhangchuangla.medicine.admin.model.request.OperationLogQueryRequest;
import cn.zhangchuangla.medicine.admin.service.SysOperationLogService;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.SysOperationLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 系统操作日志服务实现。
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Override
    public Page<SysOperationLog> logList(OperationLogQueryRequest query) {
        Page<SysOperationLog> page = query.toPage();
        return baseMapper.logList(page, query);
    }

    @Override
    public SysOperationLog getLogById(Long id) {
        Assert.isPositive(id, "日志ID必须大于0");
        return getById(id);
    }

    @Override
    public boolean clearLogs() {
        baseMapper.delete(new LambdaQueryWrapper<SysOperationLog>().isNotNull(SysOperationLog::getId));
        return true;
    }
}
