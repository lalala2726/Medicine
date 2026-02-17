package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin 端智能体用户工具接口。
 */
@RestController
@RequestMapping("/agent/user")
@Tag(name = "Admin智能体用户工具", description = "用于 Admin 侧智能体用户查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AdminAgentUserToolsController extends BaseController {

    private final UserService agentUserService;

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("hasAuthority('system:user:query') or hasRole('super_admin')")
    public AjaxResult<UserVo> getCurrentUser() {
        Long userId = getUserId();
        return success(agentUserService.getCurrentUser(userId));
    }
}
