package cn.zhangchuangla.medicine.client.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 客户端用户查询实现占位，后续接入客户端用户体系时补全。
 */
@Service
public class ClientSecurityUserService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        throw new UsernameNotFoundException("客户端用户体系暂未接入");
    }
}
