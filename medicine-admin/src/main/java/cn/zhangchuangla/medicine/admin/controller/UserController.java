package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.FreezeOrUnUserWalletRequest;
import cn.zhangchuangla.medicine.admin.model.request.WalletChangeRequest;
import cn.zhangchuangla.medicine.admin.model.vo.UserConsumeInfo;
import cn.zhangchuangla.medicine.admin.model.vo.UserDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.UserWalletFlowInfoVo;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.user.UserAddRequest;
import cn.zhangchuangla.medicine.model.request.user.UserListQueryRequest;
import cn.zhangchuangla.medicine.model.request.user.UserUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.user.UserListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/3 09:09
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "提供用户的增删改查")
@IsAdmin
public class UserController extends BaseController {

    private final UserService userService;

    /**
     * 获取用户列表
     *
     * @param request 用户列表查询参数
     * @return 用户列表视图对象集合
     */
    @GetMapping("/list")
    @Operation(summary = "用户列表")
    public AjaxResult<TableDataResult> listUser(UserListQueryRequest request) {
        Page<User> userPage = userService.listUser(request);
        List<UserListVo> userListVos = copyListProperties(userPage, UserListVo.class);
        return getTableData(userPage, userListVos);
    }


    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id:\\d+}/detail")
    @Operation(summary = "用户详情")
    public AjaxResult<UserDetailVo> getUserById(@PathVariable("id") Long id) {
        UserDetailVo userDetailVo = userService.getUserDetailById(id);
        return success(userDetailVo);
    }

    /**
     * 获取用户钱包流水
     */
    @GetMapping("/{userId}/wallet-flow")
    @Operation(summary = "获取用户钱包流水")
    public AjaxResult<TableDataResult> getUserWalletFlow(@PathVariable("userId") Long userId, PageRequest request) {
        PageResult<UserWalletFlowInfoVo> userWalletLogPage = userService.getUserWalletFlow(userId, request);
        return getTableData(userWalletLogPage);
    }

    /**
     * 获取消费信息
     */
    @GetMapping("/{userId}/consume-info")
    @Operation(summary = "获取消费信息")
    public AjaxResult<TableDataResult> getConsumeInfo(@PathVariable("userId") Long userId, PageRequest request) {
        PageResult<UserConsumeInfo> consumeInfo = userService.getConsumeInfo(userId, request);
        return getTableData(consumeInfo);
    }

    /**
     * 添加用户
     *
     * @param request 添加用户参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加用户")
    public AjaxResult<Void> addUser(@RequestBody UserAddRequest request) {
        boolean result = userService.addUser(request);
        return toAjax(result);
    }

    /**
     * 修改用户
     *
     * @param request 修改用户参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改用户")
    public AjaxResult<Void> updateUser(@RequestBody UserUpdateRequest request) {
        boolean result = userService.updateUser(request);
        return toAjax(result);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除用户")
    public AjaxResult<Void> deleteUser(@PathVariable("ids") List<Long> userId) {
        boolean result = userService.deleteUser(userId);
        return toAjax(result);
    }


    /**
     * 开通用户钱包
     *
     * @param userId 用户ID
     * @return 开通结果
     */
    @PostMapping("/wallet/open/{userId}")
    @Operation(summary = "开通用户钱包")
    public AjaxResult<Void> openUserWallet(@PathVariable("userId") Long userId) {
        boolean result = userService.openUserWallet(userId);
        return toAjax(result);
    }

    /**
     * 冻结用户钱包
     *
     * @param request 冻结用户钱包参数
     * @return 关闭结果
     */
    @PostMapping("/wallet/freeze")
    @Operation(summary = "冻结用户钱包")
    public AjaxResult<Void> closeUserWallet(@Validated @RequestBody FreezeOrUnUserWalletRequest request) {
        boolean result = userService.freezeUserWallet(request);
        return toAjax(result);
    }

    /**
     * 解冻用户钱包
     *
     * @param request 解冻用户钱包参数
     * @return 解冻结果
     */
    @PostMapping("/wallet/unfreeze")
    @Operation(summary = "解冻用户钱包")
    public AjaxResult<Void> unfreezeUserWallet(@Validated @RequestBody FreezeOrUnUserWalletRequest request) {
        boolean result = userService.unfreezeUserWallet(request);
        return toAjax(result);
    }


    /**
     * 钱包充值/扣款
     *
     * @param request 钱包充值/扣款
     * @return 钱包充值/扣款
     */
    @PostMapping("/wallet/change")
    @Operation(summary = "钱包充值/扣款")
    public AjaxResult<Void> rechargeUserWallet(@Validated @RequestBody WalletChangeRequest request) {
        boolean result = userService.walletAmountChange(request);
        return toAjax(result);
    }

}
