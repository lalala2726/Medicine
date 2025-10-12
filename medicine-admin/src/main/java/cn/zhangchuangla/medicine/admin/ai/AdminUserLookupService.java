package cn.zhangchuangla.medicine.admin.ai;

import cn.zhangchuangla.medicine.admin.ai.spi.UserLookupService;
import cn.zhangchuangla.medicine.admin.model.entity.User;
import cn.zhangchuangla.medicine.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserLookupService implements UserLookupService {

    private final UserService userService;

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(userService.getUserById(userId));
    }
}
