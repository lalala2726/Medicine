package cn.zhangchuangla.medicine.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户钱包流水 DTO。
 */
@Data
public class UserWalletFlowDto {

    private Long index;

    private String changeType;

    private BigDecimal amount;

    private Integer amountDirection;

    private Boolean isIncome;

    private BigDecimal beforeBalance;

    private BigDecimal afterBalance;

    private Date changeTime;
}
