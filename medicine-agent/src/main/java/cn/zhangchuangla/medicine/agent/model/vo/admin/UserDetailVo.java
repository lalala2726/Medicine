package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端用户详情。
 */
@Data
@Schema(description = "管理端用户详情")
@AgentVoDesc("管理端用户详情")
public class UserDetailVo {

    @Schema(description = "头像")
    @AgentFieldDesc("头像")
    private String avatar;

    @Schema(description = "昵称")
    @AgentFieldDesc("昵称")
    private String nickName;

    @Schema(description = "钱包余额")
    @AgentFieldDesc("钱包余额")
    private BigDecimal walletBalance;

    @Schema(description = "总订单数")
    @AgentFieldDesc("总订单数")
    private Integer totalOrders;

    @Schema(description = "总消费金额")
    @AgentFieldDesc("总消费金额")
    private BigDecimal totalConsume;

    @Schema(description = "基础信息")
    @AgentFieldDesc("基础信息")
    private BasicInfo basicInfo;

    @Schema(description = "安全信息")
    @AgentFieldDesc("安全信息")
    private SecurityInfo securityInfo;

    @Data
    @Schema(description = "用户基础信息")
    @AgentVoDesc("用户基础信息")
    public static class BasicInfo {

        @Schema(description = "用户ID")
        @AgentFieldDesc("用户ID")
        private Long userId;

        @Schema(description = "真实姓名")
        @AgentFieldDesc("真实姓名")
        private String realName;

        @Schema(description = "手机号")
        @AgentFieldDesc("手机号")
        private String phoneNumber;

        @Schema(description = "邮箱")
        @AgentFieldDesc("邮箱")
        private String email;

        @Schema(description = "性别")
        @AgentFieldDesc("性别")
        @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_USER_GENDER)
        private Integer gender;

        @Schema(description = "身份证号")
        @AgentFieldDesc("身份证号")
        private String idCard;
    }

    @Data
    @Schema(description = "用户安全信息")
    @AgentVoDesc("用户安全信息")
    public static class SecurityInfo {

        @Schema(description = "注册时间")
        @AgentFieldDesc("注册时间")
        private Date registerTime;

        @Schema(description = "最后登录时间")
        @AgentFieldDesc("最后登录时间")
        private Date lastLoginTime;

        @Schema(description = "最后登录IP")
        @AgentFieldDesc("最后登录IP")
        private String lastLoginIp;

        @Schema(description = "最后登录位置")
        @AgentFieldDesc("最后登录位置")
        private String lastLoginLocation;

        @Schema(description = "用户状态")
        @AgentFieldDesc("用户状态")
        @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_USER_STATUS)
        private Integer status;
    }
}
