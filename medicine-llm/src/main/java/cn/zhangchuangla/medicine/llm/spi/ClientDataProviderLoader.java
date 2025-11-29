package cn.zhangchuangla.medicine.llm.spi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * 通过 SPI 装载 client 模块的数据提供者。
 */
@Slf4j
@Component
public class ClientDataProviderLoader {

    private final ClientDataProvider provider;

    public ClientDataProviderLoader(@Autowired(required = false) ClientDataProvider providerFromContext) {
        this.provider = providerFromContext != null ? providerFromContext : loadProvider();
    }

    public Optional<ClientDataProvider> getProvider() {
        return Optional.ofNullable(provider);
    }

    private ClientDataProvider loadProvider() {
        try {
            ServiceLoader<ClientDataProvider> loader = ServiceLoader.load(ClientDataProvider.class);
            Optional<ClientDataProvider> first = loader.findFirst();
            if (first.isEmpty()) {
                log.warn("ClientDataProvider SPI implementation not found on classpath.");
            }
            return first.orElse(null);
        } catch (Exception ex) {
            log.error("Failed to load ClientDataProvider via SPI", ex);
            return null;
        }
    }
}
