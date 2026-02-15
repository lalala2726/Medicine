package cn.zhangchuangla.medicine.agent.spi.test;

import cn.zhangchuangla.medicine.agent.spi.AdminUserDataProvider;
import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * AdminUserDataProvider 测试实现。
 */
public class AdminUserDataProviderTestImpl implements AdminUserDataProvider {

    @Override
    public UserVo getCurrentUser(Long userId) {
        TestAgentSpiData.capturedAdminUserId = userId;
        return TestAgentSpiData.adminUser;
    }
}
