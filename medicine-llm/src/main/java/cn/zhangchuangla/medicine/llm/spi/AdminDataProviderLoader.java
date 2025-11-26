package cn.zhangchuangla.medicine.llm.spi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * 负责通过 SPI 装载 admin 模块提供的数据接口。
 */
@Slf4j
@Component
public class AdminDataProviderLoader {

    private final AdminDataProvider provider;

    public AdminDataProviderLoader(@Autowired(required = false) AdminDataProvider providerFromContext) {
        this.provider = providerFromContext != null ? providerFromContext : loadProvider();
    }

    public Optional<AdminDataProvider> getProvider() {
        return Optional.ofNullable(provider);
    }

    private AdminDataProvider loadProvider() {
        try {
            ServiceLoader<AdminDataProvider> loader = ServiceLoader.load(AdminDataProvider.class);
            Optional<AdminDataProvider> first = loader.findFirst();
            if (first.isEmpty()) {
                log.warn("AdminDataProvider SPI implementation not found on classpath.");
            }
            return first.orElse(null);
        } catch (Exception ex) {
            log.error("Failed to load AdminDataProvider via SPI", ex);
            return null;
        }
    }
}
