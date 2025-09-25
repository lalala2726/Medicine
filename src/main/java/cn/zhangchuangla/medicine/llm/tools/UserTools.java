package cn.zhangchuangla.medicine.llm.tools;

import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.user.UserVo;
import cn.zhangchuangla.medicine.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.service.UserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 用户相关工具,方便大模型处理用户输入
 *
 * @author Chuang
 * <p>
 * created on 2025/9/18 09:53
 */
@Component
public class UserTools {

    private final UserService userService;

    public UserTools(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Tool(name = "getCurrentUser", description = "get current user info")
    public UserVo getCurrentUser() {
        Long userId = SecurityUtils.getUserId();
        User userById = userService.getUserById(userId);
        return BeanCotyUtils.copyProperties(userById, UserVo.class);
    }

}
