package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 管理端智能体用户列表视图。
 */
@Schema(description = "管理端智能体用户列表视图")
@Data
public class AgentUserListVo {

    @Schema(description = "用户ID", format = "int64", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "角色", example = "admin")
    private String roles;

    @Schema(description = "状态（value-编码，description-描述）", example = "1")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_USER_STATUS)
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2023-01-01 00:00:00")
    private Date createTime;
}
