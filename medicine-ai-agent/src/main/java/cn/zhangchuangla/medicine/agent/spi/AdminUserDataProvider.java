package cn.zhangchuangla.medicine.agent.spi;

import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * Admin 端智能体用户数据提供者。
 */
public interface AdminUserDataProvider {

    /**
     * 获取当前用户信息。
     *
     * @param userId 当前用户 ID
     * @return 用户信息
     */
    UserVo getCurrentUser(Long userId);


    /**
     * 根据用户 ID 获取用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    AuthUserDto getUser(Long userId);
}
