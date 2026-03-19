package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.agent.model.vo.health.RpcHealthVo;
import cn.zhangchuangla.medicine.agent.service.RpcHealthService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 健康检查控制器。
 */
@RestController
@RequestMapping("/agent/health")
@Tag(name = "健康检查接口", description = "用于查看 Agent 到 admin/client 的 RPC 连通性")
@RequiredArgsConstructor
public class AgentHealthController extends BaseController {

    private final RpcHealthService rpcHealthService;

    /**
     * 查看 Agent 到 admin/client 的 RPC 链路连通性。
     *
     * @return 聚合后的 RPC 健康检查结果
     */
    @Anonymous
    @GetMapping("/rpc")
    @Operation(summary = "RPC 健康检查", description = "返回 Agent 到 admin/client 的 RPC 连通性诊断结果")
    public AjaxResult<RpcHealthVo> rpcHealth() {
        return success(rpcHealthService.checkRpcHealth());
    }
}
