package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.service.AuthService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.model.request.auth.LoginRequest;
import cn.zhangchuangla.medicine.model.request.auth.RefreshRequest;
import cn.zhangchuangla.medicine.model.request.auth.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/25 05:12
 */
@Slf4j
@RestController
@Tag(name = "客户端认证接口", description = "用户注册、登录、刷新令牌、获取个人信息")
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
     * 注册
     *
     * @param request 登录参数
     * @return 注册结果
     */
    @Anonymous
    @Operation(summary = "注册", description = "注册新用户，返回用户ID")
    @PostMapping("/register")
    public AjaxResult<Long> register(@RequestBody RegisterRequest request) {
        Long userId = authService.register(request.getUsername(), request.getPassword());
        return success(userId);
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
     * 退出登录
     *
     * @param accessToken 访问令牌
     * @return 退出结果
     */
    @Operation(summary = "退出登录", description = "清理用户会话信息")
    @PostMapping("/logout")
    public AjaxResult<Void> logout(@RequestHeader("Authorization") String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        authService.logout(accessToken);
        return success();
    }

}
