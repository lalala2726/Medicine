package cn.zhangchuangla.medicine.agent.controller.client;

import cn.zhangchuangla.medicine.agent.config.condition.ConditionalOnAgentSpi;
import cn.zhangchuangla.medicine.agent.spi.AgentSpiLoader;
import cn.zhangchuangla.medicine.agent.spi.ClientUserDataProvider;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Client 端智能体用户工具接口。
 */
@RestController
@RequestMapping("/agent/tools/client")
@Tag(name = "Client智能体用户工具", description = "用于 Client 侧智能体用户查询接口")
@ConditionalOnAgentSpi(ClientUserDataProvider.class)
public class ClientAgentUserToolsController extends BaseController {

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/current_user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public AjaxResult<UserVo> getCurrentUser() {
        Long userId = getUserId();
        ClientUserDataProvider provider = AgentSpiLoader.loadSingle(ClientUserDataProvider.class);
        return success(provider.getCurrentUser(userId));
    }
}
