package cn.zhangchuangla.medicine.ai.spi;

import cn.zhangchuangla.medicine.model.entity.User;

import java.util.Optional;

/**
 * AI 模块查询用户信息的 SPI，由具体业务侧实现。
 */
public interface UserLookupService {

    /**
     * 根据用户ID查询用户信息。
     */
    Optional<User> findById(Long userId);
}
