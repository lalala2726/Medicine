package cn.zhangchuangla.medicine.agent.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent SPI 加载器。
 */
public final class AgentSpiLoader {

    private static final Map<Class<?>, Object> SINGLE_PROVIDER_CACHE = new ConcurrentHashMap<>();

    private AgentSpiLoader() {
    }

    /**
     * 加载指定 SPI 的唯一实现。
     *
     * @param spiType SPI 类型
     * @param <T>     SPI 泛型
     * @return 唯一实现
     */
    public static <T> T loadSingle(Class<T> spiType) {
        Object cached = SINGLE_PROVIDER_CACHE.get(spiType);
        if (cached != null) {
            return spiType.cast(cached);
        }

        synchronized (SINGLE_PROVIDER_CACHE) {
            Object doubleChecked = SINGLE_PROVIDER_CACHE.get(spiType);
            if (doubleChecked != null) {
                return spiType.cast(doubleChecked);
            }

            List<T> implementations = loadAll(spiType);
            if (implementations.isEmpty()) {
                throw new IllegalStateException("No SPI implementation found for " + spiType.getName());
            }
            if (implementations.size() > 1) {
                throw new IllegalStateException(
                        "Multiple SPI implementations found for " + spiType.getName() + ": " + implementations
                );
            }

            T provider = implementations.getFirst();
            SINGLE_PROVIDER_CACHE.put(spiType, provider);
            return provider;
        }
    }

    /**
     * 判断指定 SPI 是否存在至少一个实现。
     *
     * @param spiType SPI 类型
     * @param <T>     SPI 泛型
     * @return 是否存在实现
     */
    public static <T> boolean hasImplementation(Class<T> spiType) {
        try {
            ServiceLoader<T> loader = ServiceLoader.load(spiType);
            return loader.iterator().hasNext();
        } catch (Throwable ex) {
            return false;
        }
    }

    private static <T> List<T> loadAll(Class<T> spiType) {
        List<T> result = new ArrayList<>();
        ServiceLoader<T> loader = ServiceLoader.load(spiType);
        loader.forEach(result::add);
        return result;
    }
}
