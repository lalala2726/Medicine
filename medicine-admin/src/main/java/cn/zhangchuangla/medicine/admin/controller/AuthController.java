package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.service.AuthService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.request.LoginRequest;
import cn.zhangchuangla.medicine.model.request.RefreshRequest;
import cn.zhangchuangla.medicine.model.vo.CurrentUserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 管理端认证控制器。
 * <p>
 * 提供登录、刷新令牌、当前用户信息与权限查询、登出能力。
 * </p>
 */
@Slf4j
@RestController
@Tag(name = "认证接口", description = "注册、登录、刷新、当前用户")
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录
     *
     * @param request 请求参数
     * @return 登录结果
     */
    @Anonymous
    @Operation(summary = "登录", description = "登录成功返回访问令牌与刷新令牌")
    @PostMapping("/login")
    public AjaxResult<AuthTokenVo> login(@RequestBody LoginRequest request) {
        AuthTokenVo token = authService.login(request.getUsername(), request.getPassword());
        return success(token);
    }

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌参数
     * @return 刷新令牌结果
     */
    @Anonymous
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public AjaxResult<AuthTokenVo> refresh(@RequestBody RefreshRequest request) {
        AuthTokenVo token = authService.refresh(request.getRefreshToken());
        return success(token);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据认证上下文返回当前登录用户信息")
    @GetMapping("/currentUser")
    public AjaxResult<CurrentUserInfoVo> currentUser() {
        CurrentUserInfoVo vo = authService.currentUserInfo();
        return success(vo);
    }

    /**
     * 获取当前用户权限
     *
     * @return 当前用户权限集合
     */
    @Operation(summary = "获取当前用户权限", description = "返回当前登录用户的权限编码列表")
    @GetMapping("/permissions")
    public AjaxResult<Set<String>> currentUserPermissions() {
        return success(authService.currentUserPermissions());
    }

    /**
     * 用户登出并清理当前会话。
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "登出", description = "用户登出")
    public AjaxResult<Void> logout() {
        authService.logout(SecurityUtils.getToken());
        return success();
    }

}
