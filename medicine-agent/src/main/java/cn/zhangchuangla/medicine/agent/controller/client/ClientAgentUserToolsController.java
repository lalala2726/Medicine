package cn.zhangchuangla.medicine.agent.controller.client;

import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Client 端智能体用户工具控制器。
 * <p>
 * 提供给客户端智能体使用的用户查询工具接口，
 * 用于智能体在对话过程中查询当前用户信息。
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/agent/tools/client")
@Tag(name = "Client智能体用户工具", description = "用于 Client 侧智能体用户查询接口")
@RequiredArgsConstructor
public class ClientAgentUserToolsController extends BaseController {

    private final UserService agentUserService;

    /**
     * 获取当前登录用户的详细信息。
     * <p>
     * 返回当前登录用户的完整信息，包括基本信息、头像等，
     * 供客户端智能体在服务用户时获取用户上下文。
     *
     * @return 用户详细信息
     */
    @GetMapping("/current_user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public AjaxResult<UserVo> getCurrentUser() {
        Long userId = getUserId();
        return success(agentUserService.getCurrentUser(userId));
    }
}
