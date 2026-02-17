package cn.zhangchuangla.medicine.admin.spi;

import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.agent.spi.AdminUserDataProvider;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.core.utils.SpringUtils;
import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * Admin 用户数据 SPI 实现。
 */
public class AdminUserDataProviderImpl implements AdminUserDataProvider {

    @Override
    public UserVo getCurrentUser(Long userId) {
        if (userId == null) {
            return null;
        }
        UserService userService = SpringUtils.getBean(UserService.class);
        User user = userService.getUserById(userId);
        return BeanCotyUtils.copyProperties(user, UserVo.class);
    }

    @Override
    public AuthUserDto getUser(Long userId) {
        UserService userService = SpringUtils.getBean(UserService.class);
        User user = userService.getUserById(userId);
        return BeanCotyUtils.copyProperties(user, AuthUserDto.class);
    }
}
