package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.vo.UserBriefVo;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.CurrentUserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息管理
 *
 * @author Chuang
 * created on 2025/11/12
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息管理")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据认证上下文返回当前登录用户信息")
    @GetMapping("/current")
    public AjaxResult<CurrentUserInfoVo> currentUser() {
        Long userId = SecurityUtils.getUserId();
        User user = userService.getUserById(userId);
        CurrentUserInfoVo vo = BeanCotyUtils.copyProperties(user, CurrentUserInfoVo.class);
        return success(vo);
    }

    /**
     * 获取用户简略信息
     * <p>
     * 用于个人中心页面展示，包括用户基本信息、钱包余额、订单统计等
     * </p>
     *
     * @return 用户简略信息
     */
    @GetMapping("/brief")
    @Operation(summary = "获取用户简略信息")
    public AjaxResult<UserBriefVo> getUserBriefInfo() {
        UserBriefVo userBriefVo = userService.getUserBriefInfo();
        return success(userBriefVo);
    }
}

