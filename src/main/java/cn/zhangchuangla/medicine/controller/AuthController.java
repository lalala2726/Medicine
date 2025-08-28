package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.model.request.LoginRequest;
import cn.zhangchuangla.medicine.model.request.RefreshRequest;
import cn.zhangchuangla.medicine.model.request.RegisterRequest;
import cn.zhangchuangla.medicine.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 13:31
 */
@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    /**
     * 登录
     * @param request 请求参数
     * @return 登录结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public AjaxResult<AuthTokenVo> login(@RequestBody LoginRequest request) {
        AuthTokenVo token = authService.login(request.getUsername(), request.getPassword());
        return success(token);
    }

    /**
     * 注册
     * @param request 登录参数
     * @return 注册结果
     */
    @Operation(summary = "注册")
    @PostMapping("/register")
    public AjaxResult<Long> register(@RequestBody RegisterRequest request) {
        Long userId = authService.register(request.getUsername(), request.getPassword());
        return success(userId);
    }

    /**
     * 刷新令牌
     * @param request 刷新令牌参数
     * @return 刷新令牌结果
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public AjaxResult<AuthTokenVo> refresh(@RequestBody RefreshRequest request) {
        AuthTokenVo token = authService.refresh(request.getRefreshToken());
        return success(token);
    }


}
