package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentAuthRpcService;
import cn.zhangchuangla.medicine.dubbo.api.model.AdminAuthContextDto;
import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * 智能体认证授权控制器。
 * <p>
 * 提供智能体获取当前登录用户信息的接口，用于智能体调用时确认用户身份。
 *
 * @author Chuang
 * @since 2026/2/17
 */
@RestController
@RequestMapping("/agent/authorization")
@Tag(name = "认证授权接口", description = "认证授权接口")
public class AgentAuthorizationController extends BaseController {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 3000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentAuthRpcService adminAgentAuthRpcService;

    /**
     * 获取当前登录用户的认证信息。
     * <p>
     * 返回用户的认证相关信息，包括用户ID、用户名、角色、权限等，
     * 供智能体确认调用者身份使用。
     *
     * @return 用户认证信息
     */
    @GetMapping
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public AjaxResult<HashMap<String, Object>> getCurrentUser() {
        Long userId = getUserId();
        AdminAuthContextDto context = adminAgentAuthRpcService.getByUserId(userId);
        HashMap<String, Object> userInfo = new HashMap<>();

        AuthUserDto user = context == null || context.getUser() == null
                ? null : BeanCotyUtils.copyProperties(context.getUser(), AuthUserDto.class);
        userInfo.put("user", user);
        userInfo.put("roles", context == null ? SecurityUtils.normalizeCodes(null) : SecurityUtils.normalizeCodes(context.getRoles()));
        userInfo.put("permissions", context == null ? SecurityUtils.normalizeCodes(null) : SecurityUtils.normalizeCodes(context.getPermissions()));
        return success(userInfo);
    }
}
