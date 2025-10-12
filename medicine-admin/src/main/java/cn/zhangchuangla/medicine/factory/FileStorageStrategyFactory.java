package cn.zhangchuangla.medicine.factory;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.enums.FileStorageMode;
import cn.zhangchuangla.medicine.strategy.FileStorageStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 文件存储策略工厂，负责根据配置选择具体策略实现。
 */
@Component
public class FileStorageStrategyFactory {

    private final Map<FileStorageMode, FileStorageStrategy> strategyMap;

    public FileStorageStrategyFactory(List<FileStorageStrategy> strategies) {
        this.strategyMap = new EnumMap<>(FileStorageMode.class);
        for (FileStorageStrategy strategy : strategies) {
            strategyMap.put(strategy.getMode(), strategy);
        }
    }

    /**
     * 根据存储模式获取策略实现。
     *
     * @param mode 存储模式
     * @return 策略实现
     */
    public FileStorageStrategy getStrategy(FileStorageMode mode) {
        FileStorageStrategy strategy = strategyMap.get(mode);
        if (strategy == null) {
            throw new ServiceException("Unsupported file storage mode: " + mode);
        }
        return strategy;
    }
}
