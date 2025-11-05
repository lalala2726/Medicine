package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.model.vo.UserWalletBillVo;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/6 06:37
 */
@RestController
@RequestMapping("/user/wallet")
@Tag(name = "用户钱包接口", description = "用户钱包接口")
public class UserWalletController extends BaseController {

    private final UserWalletService userWalletService;

    public UserWalletController(UserWalletService userWalletService) {
        this.userWalletService = userWalletService;
    }


    /**
     * 获取用户钱包余额
     *
     * @return 钱包余额
     */
    @GetMapping("/balance")
    @Operation(summary = "获取用户钱包余额")
    public AjaxResult<BigDecimal> getUserWalletBalance() {
        BigDecimal balance = userWalletService.getUserWalletBalance();
        return success(balance);
    }


    @GetMapping("/bill")
    @Operation(summary = "获取用户钱包流水")
    public AjaxResult<List<UserWalletBillVo>> getBillList(UserWalletBillRequest request) {
        List<UserWalletBillVo> billList = userWalletService.getBillList(request);
        return success(billList);
    }
}
