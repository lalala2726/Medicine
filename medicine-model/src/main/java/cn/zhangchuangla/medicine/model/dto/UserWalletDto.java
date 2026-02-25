package cn.zhangchuangla.medicine.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户钱包 DTO。
 */
@Data
public class UserWalletDto {

    private Long userId;

    private String walletNo;

    private BigDecimal balance;

    private BigDecimal totalIncome;

    private BigDecimal totalExpend;

    private String currency;

    private Integer status;

    private String freezeReason;

    private Date freezeTime;

    private Date updatedAt;
}
