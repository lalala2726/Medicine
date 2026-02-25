package cn.zhangchuangla.medicine.dubbo.api.client;

import cn.zhangchuangla.medicine.model.vo.UserVo;

/**
 * 客户端 Agent 用户只读 RPC。
 */
public interface ClientAgentUserRpcService {

    UserVo getCurrentUser(Long userId);
}
