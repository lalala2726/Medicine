package cn.zhangchuangla.medicine.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * 用户列表 DTO。
 */
@Data
public class UserListDto {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String roles;

    private Integer status;

    private Date createTime;
}
