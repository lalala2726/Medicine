package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.vo.admin.*;
import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import cn.zhangchuangla.medicine.model.vo.UserListVo;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端智能体用户工具控制器。
 * <p>
 * 提供给管理端智能体使用的用户查询工具接口，
 * 需要具备用户查询权限或超级管理员角色才能访问。
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/agent/admin/user")
@Tag(name = "管理端智能体用户工具", description = "用于管理端智能体用户查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AgentUserController extends BaseController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final UserService userService;

    /**
     * 获取当前管理员的详细信息。
     * <p>
     * 返回当前登录管理员的完整信息，包括基本信息、角色等，
     * 供管理端智能体在执行管理操作时获取操作者上下文。
     *
     * @return 管理员详细信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("hasAuthority('system:user:query') or hasRole('super_admin')")
    @Deprecated(forRemoval = true)
    public AjaxResult<UserVo> getCurrentUser() {
        AuthUser user = getLoginUser().getUser();
        UserVo userVo = copyProperties(user, UserVo.class);
        // 手动映射字段名不一致的属性
        userVo.setNickName(user.getNickname());
        userVo.setGender(user.getGender() == null ? null : (user.getGender() == 1 ? "男" : (user.getGender() == 2 ? "女" : "未知")));
        userVo.setStatus(user.getStatus() == null ? null : (user.getStatus() == 0 ? "正常" : "异常"));
        return success(userVo);
    }

    /**
     * 分页查询用户列表。
     *
     * @param request 用户查询参数
     * @return 用户分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "用户列表", description = "分页查询用户列表")
    @PreAuthorize("hasAuthority('system:user:list') or hasRole('super_admin')")
    public AjaxResult<TableDataResult> listUsers(UserListQueryRequest request) {
        Page<UserListDto> userPage = userService.listUsers(request);
        List<AgentUserListVo> rows = copyListProperties(userPage, AgentUserListVo.class);
        return getTableData(userPage, rows);
    }

    /**
     * 查询用户详情。
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId:\\d+}/detail")
    @Operation(summary = "用户详情", description = "根据用户ID获取用户详情")
    @PreAuthorize("hasAuthority('system:user:query') or hasRole('super_admin')")
    public AjaxResult<UserDetailVo> getUserDetail(@PathVariable Long userId) {
        UserDetailDto userDetailDto = userService.getUserDetailById(userId);
        return success(toUserDetailVo(userDetailDto));
    }

    /**
     * 查询用户钱包信息。
     *
     * @param userId 用户ID
     * @return 用户钱包信息
     */
    @GetMapping("/{userId:\\d+}/wallet")
    @Operation(summary = "用户钱包", description = "根据用户ID获取钱包余额与状态")
    @PreAuthorize("hasAuthority('system:user:query') or hasRole('super_admin')")
    public AjaxResult<UserWalletVo> getUserWallet(@PathVariable Long userId) {
        UserWalletDto userWalletDto = userService.getUserWalletByUserId(userId);
        return success(toUserWalletVo(userWalletDto));
    }

    /**
     * 分页查询用户钱包流水。
     *
     * @param userId  用户ID
     * @param request 分页参数
     * @return 钱包流水分页列表
     */
    @GetMapping("/{userId:\\d+}/wallet_flow")
    @Operation(summary = "用户钱包流水", description = "分页查询用户钱包流水")
    @PreAuthorize("hasAuthority('system:user:query') or hasRole('super_admin')")
    public AjaxResult<TableDataResult> getUserWalletFlow(@PathVariable Long userId, PageRequest request) {
        Page<UserWalletFlowDto> walletFlowDtoPage = userService.getUserWalletFlow(userId, request);
        List<UserWalletFlowVo> rows = copyListProperties(walletFlowDtoPage, UserWalletFlowVo.class);
        return getTableData(walletFlowDtoPage, rows);
    }

    /**
     * 分页查询用户消费信息。
     *
     * @param userId  用户ID
     * @param request 分页参数
     * @return 用户消费分页列表
     */
    @GetMapping("/{userId:\\d+}/consume_info")
    @Operation(summary = "用户消费信息", description = "分页查询用户消费信息")
    @PreAuthorize("hasAuthority('system:user:query') or hasRole('super_admin')")
    public AjaxResult<TableDataResult> getConsumeInfo(@PathVariable Long userId, PageRequest request) {
        Page<UserConsumeInfoDto> consumeInfoDtoPage = userService.getConsumeInfo(userId, request);
        List<UserConsumeInfoVo> rows = copyListProperties(consumeInfoDtoPage.getRecords(), UserConsumeInfoVo.class);
        return getTableData(consumeInfoDtoPage, rows);
    }

    private UserDetailVo toUserDetailVo(UserDetailDto source) {
        if (source == null) {
            return null;
        }
        UserDetailVo target = copyProperties(source, UserDetailVo.class);
        target.setBasicInfo(copyProperties(source.getBasicInfo(), UserDetailVo.BasicInfo.class));
        target.setSecurityInfo(copyProperties(source.getSecurityInfo(), UserDetailVo.SecurityInfo.class));
        return target;
    }

    private UserWalletVo toUserWalletVo(UserWalletDto source) {
        return copyProperties(source, UserWalletVo.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<?> toUserListRows(List<UserListDto> sourceRows) {
        if (sourceRows == null || sourceRows.isEmpty()) {
            return List.of();
        }
        Class<?> targetClass = resolveUserListVoClass();
        return copyListProperties(sourceRows, (Class) targetClass);
    }

    private Class<?> resolveUserListVoClass() {
        try {
            return Class.forName("cn.zhangchuangla.medicine.agent.model.vo.admin.AgentUserListVo");
        } catch (ClassNotFoundException | LinkageError ex) {
            return UserListVo.class;
        }
    }
}
