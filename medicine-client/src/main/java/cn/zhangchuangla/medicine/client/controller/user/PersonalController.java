package cn.zhangchuangla.medicine.client.controller.user;

import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.user.CurrentUserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/28 20:50
 */
@RestController
@RequestMapping("/personal")
@Tag(name = "个人中心", description = "个人中心接口")
public class PersonalController extends BaseController {

    private final UserService userService;

    public PersonalController(UserService userService) {
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
}
