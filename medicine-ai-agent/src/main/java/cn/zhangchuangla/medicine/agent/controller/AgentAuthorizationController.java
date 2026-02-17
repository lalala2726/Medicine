package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.agent.spi.AdminUserDataProvider;
import cn.zhangchuangla.medicine.agent.spi.AgentSpiLoader;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2026/2/17
 */
@RestController
@RequestMapping("/agent/authorization")
@Tag(name = "认证授权接口", description = "认证授权接口")
public class AgentAuthorizationController extends BaseController {

    /**
     * 获取当前用户信息。
     */
    @GetMapping
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public AjaxResult<AuthUserDto> getCurrentUser() {
        Long userId = getUserId();
        AdminUserDataProvider provider = AgentSpiLoader.loadSingle(AdminUserDataProvider.class);
        return success(provider.getUser(userId));
    }
}
