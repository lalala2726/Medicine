package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentUserRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 用户服务 Dubbo Consumer 实现。
 */
@Service
public class UserServiceImpl implements UserService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 5000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentUserRpcService adminAgentUserRpcService;

    @Override
    public Page<UserListDto> listUsers(UserListQueryRequest request) {
        UserListQueryRequest safeRequest = request == null ? new UserListQueryRequest() : request;
        PageResult<UserListDto> result = adminAgentUserRpcService.listUsers(safeRequest);
        return toPage(result);
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
    public PageResult<UserWalletFlowDto> getUserWalletFlow(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        return adminAgentUserRpcService.getUserWalletFlow(userId, safeRequest);
    }

    @Override
    public PageResult<UserConsumeInfoDto> getConsumeInfo(Long userId, PageRequest request) {
        PageRequest safeRequest = request == null ? new PageRequest() : request;
        return adminAgentUserRpcService.getConsumeInfo(userId, safeRequest);
    }

    private Page<UserListDto> toPage(PageResult<UserListDto> result) {
        if (result == null) {
            return new Page<>(1, 10, 0);
        }
        long pageNum = result.getPageNum() == null ? 1L : result.getPageNum();
        long pageSize = result.getPageSize() == null ? 10L : result.getPageSize();
        long total = result.getTotal() == null ? 0L : result.getTotal();

        Page<UserListDto> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(result.getRows() == null ? List.of() : result.getRows());
        return page;
    }
}
