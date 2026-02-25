package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 智能体用户服务接口。
 */
public interface UserService {

    /**
     * 分页查询用户列表。
     *
     * @param request 用户查询参数
     * @return 用户分页列表
     */
    Page<UserListDto> listUsers(UserListQueryRequest request);

    /**
     * 根据用户 ID 查询用户详情。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    UserDetailDto getUserDetailById(Long userId);

    /**
     * 根据用户 ID 查询钱包信息。
     *
     * @param userId 用户 ID
     * @return 钱包信息
     */
    UserWalletDto getUserWalletByUserId(Long userId);

    /**
     * 分页查询用户钱包流水。
     *
     * @param userId 用户 ID
     * @param request 分页参数
     * @return 钱包流水分页结果
     */
    Page<UserWalletFlowDto> getUserWalletFlow(Long userId, PageRequest request);

    /**
     * 分页查询用户消费信息。
     *
     * @param userId 用户 ID
     * @param request 分页参数
     * @return 用户消费分页结果
     */
    Page<UserConsumeInfoDto> getConsumeInfo(Long userId, PageRequest request);
}
