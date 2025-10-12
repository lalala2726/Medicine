package cn.zhangchuangla.medicine.admin.ai.tools;

import cn.zhangchuangla.medicine.admin.ai.spi.UserLookupService;
import cn.zhangchuangla.medicine.admin.ai.workflow.context.UserContextHolder;
import cn.zhangchuangla.medicine.admin.ai.workflow.progress.WorkflowProgressContextHolder;
import cn.zhangchuangla.medicine.admin.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.admin.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.admin.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.admin.model.entity.User;
import cn.zhangchuangla.medicine.admin.model.vo.user.UserVo;
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

    private final UserLookupService userLookupService;

    public UserTools(UserLookupService userLookupService) {
        this.userLookupService = userLookupService;
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
        if (userDetails != null) {
            userId = userDetails.getUserId();
        } else {
            throw new ServiceException("当前用户未登录");
        }
        User user = userLookupService.findById(userId)
                .orElseThrow(() -> new ServiceException("当前用户不存在"));
        UserVo userVo = BeanCotyUtils.copyProperties(user, UserVo.class);
        WorkflowProgressContextHolder.publishToolResult(tool, userVo != null ? "查询成功" : "未查询到用户信息");
        return userVo;
    }
}
