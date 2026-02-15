package cn.zhangchuangla.medicine.agent.spi;

import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * Client 端智能体用户数据提供者。
 */
public interface ClientUserDataProvider {

    /**
     * 获取当前用户信息。
     *
     * @param userId 当前用户 ID
     * @return 用户信息
     */
    UserVo getCurrentUser(Long userId);
}
