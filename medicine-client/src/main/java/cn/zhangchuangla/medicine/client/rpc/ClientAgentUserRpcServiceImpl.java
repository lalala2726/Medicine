package cn.zhangchuangla.medicine.client.rpc;

import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.dubbo.api.client.ClientAgentUserRpcService;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 客户端 Agent 用户 RPC Provider。
 */
@DubboService(interfaceClass = ClientAgentUserRpcService.class, group = "medicine-client", version = "1.0.0")
@RequiredArgsConstructor
public class ClientAgentUserRpcServiceImpl implements ClientAgentUserRpcService {

    private final UserService userService;

    @Override
    public UserVo getCurrentUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userService.getUserById(userId);
        return BeanCotyUtils.copyProperties(user, UserVo.class);
    }
}
