package cn.zhangchuangla.medicine.ai;

import cn.zhangchuangla.medicine.ai.spi.UserLookupService;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.service.UserService;
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
