package cn.zhangchuangla.medicine.client.spi;

import cn.zhangchuangla.medicine.agent.spi.ClientUserDataProvider;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.core.utils.SpringUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * Client 用户数据 SPI 实现。
 */
public class ClientUserDataProviderImpl implements ClientUserDataProvider {

    @Override
    public UserVo getCurrentUser(Long userId) {
        if (userId == null) {
            return null;
        }
        UserService userService = SpringUtils.getBean(UserService.class);
        User user = userService.getUserById(userId);
        return BeanCotyUtils.copyProperties(user, UserVo.class);
    }
}
