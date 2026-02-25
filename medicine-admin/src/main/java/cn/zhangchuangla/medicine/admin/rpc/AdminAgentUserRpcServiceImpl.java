package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
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
    public PageResult<UserListDto> listUsers(UserListQueryRequest query) {
        UserListQueryRequest request = query == null ? new UserListQueryRequest() : query;
        Page<UserListDto> userPage = userService.listUser(request);
        return new PageResult<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal(), userPage.getRecords());
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
    public PageResult<UserWalletFlowDto> getUserWalletFlow(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        Page<UserWalletFlowDto> page = userService.getUserWalletFlow(userId, safeRequest);
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public PageResult<UserConsumeInfoDto> getConsumeInfo(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        Page<UserConsumeInfoDto> page = userService.getConsumeInfo(userId, safeRequest);
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
