package cn.zhangchuangla.medicine.rpc.client;

import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * 客户端 Agent 用户只读 RPC。
 */
public interface ClientAgentUserRpcService {

    /**
     * 根据用户 ID 查询当前用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息；未命中时返回 {@code null}
     */
    UserVo getCurrentUser(Long userId);
}
