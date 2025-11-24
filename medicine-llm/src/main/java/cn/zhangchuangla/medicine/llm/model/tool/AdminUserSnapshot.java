package cn.zhangchuangla.medicine.llm.model.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * 精简的用户画像，供 LLM 工具调用返回。
 */
@Data
@Schema(description = "AI 工具可用的用户信息快照")
public class AdminUserSnapshot {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "登录名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色集合")
    private Set<String> roles;

    @Schema(description = "最近一次登录时间")
    private Date lastLoginTime;

    @Schema(description = "最近一次登录 IP")
    private String lastLoginIp;

    @Schema(description = "累计下单次数")
    private Long totalOrders;

    @Schema(description = "累计消费金额")
    private BigDecimal totalConsume;

    @Schema(description = "当前钱包余额")
    private BigDecimal walletBalance;
}
