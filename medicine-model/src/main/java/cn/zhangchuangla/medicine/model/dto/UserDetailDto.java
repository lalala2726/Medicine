package cn.zhangchuangla.medicine.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户详情 DTO。
 */
@Data
public class UserDetailDto {

    private String avatar;

    private String nickName;

    private BigDecimal walletBalance;

    private Integer totalOrders;

    private BigDecimal totalConsume;

    private BasicInfo basicInfo;

    private SecurityInfo securityInfo;

    @Data
    public static class BasicInfo {

        private Long userId;

        private String realName;

        private String phoneNumber;

        private String email;

        private Integer gender;

        private String idCard;
    }

    @Data
    public static class SecurityInfo {

        private Date registerTime;

        private Date lastLoginTime;

        private String lastLoginIp;

        private String lastLoginLocation;

        private Integer status;
    }
}
