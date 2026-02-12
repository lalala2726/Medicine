package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.dto.UserProfileDto;
import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.model.vo.UserBriefVo;
import cn.zhangchuangla.medicine.client.model.vo.UserWalletBillVo;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import cn.zhangchuangla.medicine.model.vo.CurrentUserInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端用户控制器。
 * <p>
 * 提供个人资料、当前用户信息、钱包与订单统计相关接口。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息管理")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final UserWalletService userWalletService;


    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/profile")
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
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息")
    public AjaxResult<Void> updateUserProfile(@RequestBody UserProfileDto userProfileDto) {
        boolean result = userService.updateUserProfile(userProfileDto);
        return toAjax(result);
    }

    /**
     * 获取当前登录用户信息（包含角色集合）。
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据认证上下文返回当前登录用户信息")
    @GetMapping("/current")
    public AjaxResult<CurrentUserInfoVo> currentUser() {
        Long userId = SecurityUtils.getUserId();
        User user = userService.getUserById(userId);
        CurrentUserInfoVo vo = BeanCotyUtils.copyProperties(user, CurrentUserInfoVo.class);
        vo.setRoles(userService.getUserRolesByUserId(userId));
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

    /**
     * 获取用户钱包余额
     *
     * @return 钱包余额
     */
    @GetMapping("/wallet/balance")
    @Operation(summary = "获取用户钱包余额")
    public AjaxResult<BigDecimal> getUserWalletBalance() {
        BigDecimal balance = userWalletService.getUserWalletBalance();
        return success(balance);
    }


    /**
     * 获取用户钱包流水
     *
     * @param request 查询参数
     * @return 流水列表
     */
    @GetMapping("/wallet/bill")
    @Operation(summary = "获取用户钱包流水")
    public AjaxResult<TableDataResult> getBillList(UserWalletBillRequest request) {
        Page<UserWalletLog> walletLogPage = userWalletService.getBillList(request);
        AtomicLong counter = new AtomicLong(1);
        ArrayList<UserWalletBillVo> userWalletBillVos = new ArrayList<>();
        walletLogPage.getRecords().forEach(walletLog -> {
            UserWalletBillVo userService = UserWalletBillVo.builder()
                    .index(counter.getAndIncrement())
                    .isRecharge(walletLog.getChangeType() == 1)
                    .title(walletLog.getReason())
                    .amount(walletLog.getAmount())
                    .time(walletLog.getCreatedAt())
                    .build();
            userWalletBillVos.add(userService);
        });
        return getTableData(walletLogPage, userWalletBillVos);
    }
}
