package cn.zhangchuangla.medicine.common.security.example;

import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员权限使用示例控制器
 * <p>
 * 该控制器演示了如何使用 {@link IsAdmin} 注解来保护需要管理员权限的接口。
 * 包含了不同场景下的使用示例，供开发参考。
 * </p>
 *
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>方法级别的权限控制</li>
 *   <li>类级别的权限控制</li>
 *   <li>自定义管理员角色标识</li>
 *   <li>自定义错误消息</li>
 * </ul>
 *
 * <p><strong>注意事项：</strong></p>
 * <ul>
 *   <li>这是示例代码，仅用于演示注解的使用方法</li>
 *   <li>在实际项目中，请根据业务需求调整权限控制策略</li>
 *   <li>确保用户认证和授权系统正常工作</li>
 * </ul>
 *
 * @author Chuang
 * @since 1.0.0
 * created on 2025/10/27
 */
@RestController
@RequestMapping("/example/admin")
public class AdminExampleController {

    private static final Logger log = LoggerFactory.getLogger(AdminExampleController.class);

    /**
     * 获取管理员面板信息 - 需要默认管理员权限
     *
     * @return 管理员面板数据
     */
    @GetMapping("/dashboard")
    @IsAdmin
    public AjaxResult<Map<String, Object>> getAdminDashboard() {
        log.info("管理员[{}]访问管理员面板", SecurityUtils.getUsername());

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsers", 1000);
        dashboard.put("totalOrders", 5000);
        dashboard.put("totalRevenue", 100000.0);
        dashboard.put("systemStatus", "正常");

        return AjaxResult.success("获取管理员面板信息成功", dashboard);
    }

    /**
     * 创建用户 - 需要默认管理员权限
     *
     * @param userInfo 用户信息
     * @return 创建结果
     */
    @PostMapping("/users")
    @IsAdmin(message = "只有管理员才能创建用户")
    public AjaxResult<Void> createUser(@RequestBody Map<String, Object> userInfo) {
        log.info("管理员[{}]创建用户: {}", SecurityUtils.getUsername(), userInfo.get("username"));

        // 这里应该是创建用户的业务逻辑
        // 示例代码仅演示权限控制

        return AjaxResult.success("用户创建成功");
    }

    /**
     * 删除用户 - 需要默认管理员权限
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/users/{userId}")
    @IsAdmin
    public AjaxResult<Void> deleteUser(@PathVariable Long userId) {
        log.info("管理员[{}]删除用户: {}", SecurityUtils.getUsername(), userId);

        // 这里应该是删除用户的业务逻辑
        // 示例代码仅演示权限控制

        return AjaxResult.success("用户删除成功");
    }

    /**
     * 获取系统配置 - 需要超级管理员权限
     *
     * @return 系统配置信息
     */
    @GetMapping("/system/config")
    @IsAdmin(value = "super_admin", message = "只有超级管理员才能访问系统配置")
    public AjaxResult<Map<String, Object>> getSystemConfig() {
        log.info("超级管理员[{}]访问系统配置", SecurityUtils.getUsername());

        Map<String, Object> config = new HashMap<>();
        config.put("systemName", "医疗管理系统");
        config.put("version", "1.0.0");
        config.put("environment", "production");
        config.put("maxUsers", 10000);

        return AjaxResult.success("获取系统配置成功", config);
    }

    /**
     * 查看当前用户信息 - 不需要管理员权限（公开接口）
     *
     * @return 当前用户信息
     */
    @GetMapping("/current-user")
    public AjaxResult<Map<String, Object>> getCurrentUser() {
        Map<String, Object> userInfo = new HashMap<>();

        if (SecurityUtils.isAuthenticated()) {
            userInfo.put("username", SecurityUtils.getUsername());
            userInfo.put("userId", SecurityUtils.getUserId());
            userInfo.put("roles", SecurityUtils.getRoles());
            userInfo.put("isAdmin", SecurityUtils.isAdmin());
        } else {
            userInfo.put("username", "未登录");
            userInfo.put("isAdmin", false);
        }

        return AjaxResult.success("获取用户信息成功", userInfo);
    }

    /**
     * 管理员专用操作 - 需要默认管理员权限
     *
     * @return 操作结果
     */
    @PostMapping("/admin-operation")
    @IsAdmin
    public AjaxResult<String> performAdminOperation(@RequestBody Map<String, Object> params) {
        log.info("管理员[{}]执行管理员操作: {}", SecurityUtils.getUsername(), params.get("operation"));

        // 这里应该是管理员专用操作的逻辑
        // 示例代码仅演示权限控制

        return AjaxResult.success("管理员操作执行成功");
    }
}