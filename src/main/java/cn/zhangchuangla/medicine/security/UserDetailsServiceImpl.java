package cn.zhangchuangla.medicine.security;

import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义用户详情服务实现
 * 用于Spring Security认证过程中加载用户信息
 *
 * @author Chuang
 * <p>
 * created on 2025/2/19 13:34
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    /**
     * 根据用户名获取用户信息
     * 此方法会在用户尝试登录时由Spring Security调用
     *
     * @param username 用户名
     * @return UserDetails 用户详情对象
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        try {
            // 获取系统用户信息
            User user = userService.getUserByUsername(username);
            if (user == null) {
                log.warn("用户[{}]不存在", username);
                throw new UsernameNotFoundException("用户不存在");
            }
            return new SysUserDetails(user);
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载用户[{}]信息时发生错误", username, e);
            throw e;
        }
    }
}

