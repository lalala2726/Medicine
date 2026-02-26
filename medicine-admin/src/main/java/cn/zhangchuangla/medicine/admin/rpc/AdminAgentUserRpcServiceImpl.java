package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentUserRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 管理端 Agent 用户 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentUserRpcService.class, group = "medicine-admin", version = "1.0.0")
@RequiredArgsConstructor
public class AdminAgentUserRpcServiceImpl implements AdminAgentUserRpcService {

    private final UserService userService;

    @Override
    public Page<UserListDto> listUsers(UserListQueryRequest query) {
        return userService.listUser(query);
    }

    @Override
    public UserDetailDto getUserDetailById(Long userId) {
        return userService.getUserDetailById(userId);
    }

    @Override
    public UserWalletDto getUserWalletByUserId(Long userId) {
        return userService.getUserWallet(userId);
    }

    @Override
    public Page<UserWalletFlowDto> getUserWalletFlow(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        return userService.getUserWalletFlow(userId, safeRequest);
    }

    @Override
    public Page<UserConsumeInfoDto> getConsumeInfo(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        return userService.getConsumeInfo(userId, safeRequest);
    }
}
