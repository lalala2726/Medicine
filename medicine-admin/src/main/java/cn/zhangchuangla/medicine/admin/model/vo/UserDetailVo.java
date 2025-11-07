package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户
 */
@Schema(description = "用户信息视图对象")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailVo {

    /**
     * 头像
     */
    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickName;

    /**
     * 钱包余额
     */
    @Schema(description = "钱包余额", example = "100.00")
    private BigDecimal walletBalance;

    /**
     * 总订单数
     */
    @Schema(description = "总订单数", example = "10")
    private Integer totalOrders;

    /**
     * 总消费金额
     */
    @Schema(description = "总消费金额", example = "500.00")
    private BigDecimal totalConsume;

    /**
     * 基础信息
     */
    @Schema(description = "基础信息")
    private BasicInfo basicInfo;

    /**
     * 安全信息
     */
    @Schema(description = "安全信息")
    private SecurityInfo securityInfo;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "用户基础信息视图对象")
    public static class BasicInfo {

        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "1")
        private Long userId;

        /**
         * 用户名
         */
        @Schema(description = "用户名", example = "zhangsan")
        private String realName;

        /**
         * 手机号
         */
        @Schema(description = "手机号", example = "13800000000")
        private String phoneNumber;

        /**
         * 邮箱
         */
        @Schema(description = "邮箱", example = "zhangsan@example.com")
        private String email;

        /**
         * 性别
         */
        @Schema(description = "性别", example = "男")
        private Integer gender;

        /**
         * 身份证号
         */
        @Schema(description = "身份证号", example = "110101199001011234")
        private String idCard;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "用户安全信息视图对象")
    public static class SecurityInfo {

        /**
         * 注册时间
         */
        @Schema(description = "注册时间", example = "2025-11-07 14:31:00")
        private Date registerTime;

        /**
         * 最后登录时间
         */
        @Schema(description = "最后登录时间", example = "2025-11-07 14:31:00")
        private Date lastLoginTime;

        /**
         * 最后登录IP
         */
        @Schema(description = "最后登录IP", example = "192.168.1.1")
        private String lastLoginIp;

        /**
         * 最后登录位置
         */
        @Schema(description = "最后登录位置", example = "中国北京")
        private String lastLoginLocation;

        /**
         * 用户状态
         */
        @Schema(description = "用户状态", example = "正常")
        private Integer status;
    }

}
