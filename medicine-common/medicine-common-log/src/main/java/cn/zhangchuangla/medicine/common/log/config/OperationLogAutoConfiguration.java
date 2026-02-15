package cn.zhangchuangla.medicine.common.log.config;

import cn.zhangchuangla.medicine.common.log.aspect.OperationLogAspect;
import cn.zhangchuangla.medicine.common.log.executor.OperationLogExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 操作日志自动配置。
 */
@AutoConfiguration
public class OperationLogAutoConfiguration {

    @Bean
    public OperationLogExecutor operationLogExecutor() {
        return new OperationLogExecutor();
    }

    @Bean
    public OperationLogAspect operationLogAspect(OperationLogExecutor operationLogExecutor) {
        return new OperationLogAspect(operationLogExecutor);
    }
}
