package cn.zhangchuangla.medicine.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户消费信息 DTO。
 */
@Data
public class UserConsumeInfoDto {

    private Long index;

    private Long userId;

    private String orderNo;

    private BigDecimal totalPrice;

    private BigDecimal payPrice;

    private Date finishTime;
}
