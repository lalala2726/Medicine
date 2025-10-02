package cn.zhangchuangla.medicine.llm.tools;

import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.llm.workflow.progress.WorkflowProgressContextHolder;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.user.UserVo;
import cn.zhangchuangla.medicine.security.context.UserContextHolder;
import cn.zhangchuangla.medicine.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.service.UserService;
import cn.zhangchuangla.medicine.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 用户相关工具,方便大模型处理用户输入
 *
 * @author Chuang
 * <p>
 * created on 2025/9/18 09:53
 */
@Slf4j
@Component
public class UserTools {

    private final UserService userService;

    public UserTools(UserService userService) {
        this.userService = userService;
    }

    /**
     * 清除当前用户信息
     */
    public void clearCurrentUser() {
        UserContextHolder.clear();
        log.info("UserTools中清除当前用户信息");
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Tool(name = "getCurrentUser", description = "get current user info")
    public UserVo getCurrentUser() {
        final String tool = "getCurrentUser";
        WorkflowProgressContextHolder.publishToolInvoke(tool, "正在查询当前用户信息");

        Long userId;
        SysUserDetails userDetails = UserContextHolder.get();
        log.info("ThreadLocal中用户信息为: {}", userDetails);

        if (userDetails != null) {
            // 使用ThreadLocal中的用户信息
            userId = userDetails.getUserId();
            log.info("从ThreadLocal获取用户信息: {}", userDetails.getUsername());
        } else {
            // 回退到SecurityUtils获取（用于直接调用的情况）
            try {
                userId = SecurityUtils.getUserId();
                log.info("从SecurityContext获取用户信息");
            } catch (Exception e) {
                log.warn("无法获取当前用户信息: {}", e.getMessage());
                WorkflowProgressContextHolder.publishToolResult(tool, "用户未登录");
                return null;
            }
        }

        User userById = userService.getUserById(userId);
        UserVo userVo = BeanCotyUtils.copyProperties(userById, UserVo.class);
        WorkflowProgressContextHolder.publishToolResult(tool, userVo != null ? "查询成功" : "未查询到用户信息");
        return userVo;
    }

    /**
     * 设置当前用户信息（由workflow节点调用）
     *
     * @param userDetails 当前用户详情
     */
    public void setCurrentUser(SysUserDetails userDetails) {
        UserContextHolder.set(userDetails);
        log.info("UserTools中设置当前用户: {}", userDetails.getUsername());
    }

}
