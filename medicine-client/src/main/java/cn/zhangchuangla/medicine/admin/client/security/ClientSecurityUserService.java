package cn.zhangchuangla.medicine.admin.client.security;

import cn.zhangchuangla.medicine.admin.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.admin.common.security.spi.SecurityUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 客户端用户查询实现占位，后续接入客户端用户体系时补全。
 */
@Service
public class ClientSecurityUserService implements SecurityUserService {

    @Override
    public Optional<AuthUser> loadUserByUsername(String username) {
        return Optional.empty();
    }
}
