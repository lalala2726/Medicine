package cn.zhangchuangla.medicine.agent.spi.test;

import cn.zhangchuangla.medicine.agent.spi.ClientUserDataProvider;
import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * ClientUserDataProvider 测试实现。
 */
public class ClientUserDataProviderTestImpl implements ClientUserDataProvider {

    @Override
    public UserVo getCurrentUser(Long userId) {
        TestAgentSpiData.capturedClientUserId = userId;
        return TestAgentSpiData.clientUser;
    }
}
