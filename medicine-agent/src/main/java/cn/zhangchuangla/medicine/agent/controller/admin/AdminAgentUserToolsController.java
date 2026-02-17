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
 * Admin 端智能体用户工具控制器。
 * <p>
 * 提供给管理端智能体使用的用户查询工具接口，
 * 需要具备用户查询权限或超级管理员角色才能访问。
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/agent/user")
@Tag(name = "Admin智能体用户工具", description = "用于 Admin 侧智能体用户查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AdminAgentUserToolsController extends BaseController {

    private final UserService agentUserService;

    /**
     * 获取当前管理员的详细信息。
     * <p>
     * 返回当前登录管理员的完整信息，包括基本信息、角色等，
     * 供管理端智能体在执行管理操作时获取操作者上下文。
     *
     * @return 管理员详细信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("hasAuthority('system:user:query') or hasRole('super_admin')")
    public AjaxResult<UserVo> getCurrentUser() {
        Long userId = getUserId();
        return success(agentUserService.getCurrentUser(userId));
    }
}
