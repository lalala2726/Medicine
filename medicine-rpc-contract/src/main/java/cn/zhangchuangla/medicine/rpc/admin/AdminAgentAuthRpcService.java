package cn.zhangchuangla.medicine.rpc.admin;


import cn.zhangchuangla.medicine.model.dto.AuthContextDto;

/**
 * 管理端 Agent 认证上下文 RPC。
 */
public interface AdminAgentAuthRpcService {

    /**
     * 根据用户 ID 查询管理端智能体认证上下文。
     *
     * @param userId 用户 ID
     * @return 认证上下文；未命中时返回 {@code null}
     */
    AuthContextDto getByUserId(Long userId);

    /**
     * 根据用户名查询管理端智能体认证上下文。
     *
     * @param username 用户名
     * @return 认证上下文；未命中时返回 {@code null}
     */
    AuthContextDto getByUsername(String username);
}
