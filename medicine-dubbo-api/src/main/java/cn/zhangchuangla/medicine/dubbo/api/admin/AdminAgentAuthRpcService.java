package cn.zhangchuangla.medicine.dubbo.api.admin;

import cn.zhangchuangla.medicine.dubbo.api.model.AdminAuthContextDto;

/**
 * 管理端 Agent 认证上下文 RPC。
 */
public interface AdminAgentAuthRpcService {

    AdminAuthContextDto getByUserId(Long userId);

    AdminAuthContextDto getByUsername(String username);
}
