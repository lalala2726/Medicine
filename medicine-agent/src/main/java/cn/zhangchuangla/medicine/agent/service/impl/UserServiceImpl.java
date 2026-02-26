package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentUserRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * Agent 用户服务 Dubbo Consumer 实现。
 */
@Service
public class UserServiceImpl implements UserService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 10000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentUserRpcService adminAgentUserRpcService;

    @Override
    public Page<UserListDto> listUsers(UserListQueryRequest request) {
        return adminAgentUserRpcService.listUsers(request);
    }

    @Override
    public UserDetailDto getUserDetailById(Long userId) {
        return adminAgentUserRpcService.getUserDetailById(userId);
    }

    @Override
    public UserWalletDto getUserWalletByUserId(Long userId) {
        return adminAgentUserRpcService.getUserWalletByUserId(userId);
    }

    @Override
    public Page<UserWalletFlowDto> getUserWalletFlow(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        return adminAgentUserRpcService.getUserWalletFlow(userId, safeRequest);
    }

    @Override
    public Page<UserConsumeInfoDto> getConsumeInfo(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        return adminAgentUserRpcService.getConsumeInfo(userId, safeRequest);
    }
}
