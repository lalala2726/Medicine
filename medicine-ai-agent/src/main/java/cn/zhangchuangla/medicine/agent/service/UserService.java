package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;

import java.util.Set;

public interface UserService {

    UserVo getCurrentUser(Long userId);

    AuthUserDto getUser(Long userId);

    User getUserByUsername(String username);

    Set<String> getUserRolesByUserId(Long userId);

    Set<String> getUserPermissionCodesByUserId(Long userId);
}
