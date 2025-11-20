package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.dto.UserProfileDto;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.*;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/20
 */
@RestController
@RequestMapping("/user/profile")
@Schema(description = "用户信息")
public class UserProfileController extends BaseController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }


    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping
    @Operation(summary = "获取用户信息")
    public AjaxResult<UserProfileDto> getUserProfile() {
        UserProfileDto userProfileDto = userService.getUserProfile();
        return success(userProfileDto);
    }

    /**
     * 更新用户信息
     *
     * @param userProfileDto 用户信息
     * @return 更新结果
     */
    @PutMapping
    @Operation(summary = "更新用户信息")
    public AjaxResult<Void> updateUserProfile(@RequestBody UserProfileDto userProfileDto) {
        boolean result = userService.updateUserProfile(userProfileDto);
        return toAjax(result);
    }

}
