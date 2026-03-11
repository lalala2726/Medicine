package cn.zhangchuangla.medicine.common.core.constants;

/**
 * Redis 常量
 *
 * @author Chuang
 */
public interface RedisConstants {
    /**
     * 验证码前缀
     */
    String CAPTCHA_CODE = "captcha:code:";

    /**
     * 接口访问限流前缀
     */
    String ACCESS_LIMIT_PREFIX = "access_limit:";

    /**
     * IP限流前缀
     */
    String ACCESS_LIMIT_IP = ACCESS_LIMIT_PREFIX + "ip:";

    /**
     * 用户ID限流前缀
     */
    String ACCESS_LIMIT_USER = ACCESS_LIMIT_PREFIX + "user:";

    /**
     * 自定义限流前缀
     */
    String ACCESS_LIMIT_CUSTOM = ACCESS_LIMIT_PREFIX + "custom:";

    /**
     * 字典模块缓存接口
     */
    interface Dict {
        /**
         * 字典缓存前缀
         */
        String DICT_CACHE_PREFIX = "system:dict:data:";

        /**
         * 字典数据缓存Key格式: system:dict:data:{dictType}
         */
        String DICT_DATA_KEY = DICT_CACHE_PREFIX + "%s";

        /**
         * 字典缓存过期时间（秒）- 24小时
         */
        int DICT_CACHE_EXPIRE_TIME = 24 * 60 * 60;
    }


    interface StorageConfig {
        String ACTIVE_TYPE = "storage:active_type";
        String CURRENT_STORAGE_CONFIG = "storage:current_storage_config";
        String CONFIGURATION_FILE_TYPE = "storage:configuration_file_type";
        String CONFIG_TYPE_DATABASE = "database";
    }


    /**
     * 认证模块
     */
    interface Auth {

        String USER_ACCESS_TOKEN = "auth:token:access:";

        String USER_REFRESH_TOKEN = "auth:token:refresh:";

        String ROLE_KEY = "auth:role:";

        String SESSIONS_INDEX_KEY = "auth:session:index:";

        String SESSIONS_DEVICE_KEY = "auth:session:device:";

        /**
         * 密码重试限制前缀
         */
        String PASSWORD_RETRY_PREFIX = "auth:password:retry:";

        /**
         * 密码重试次数缓存Key格式: auth:password:retry:{username}
         */
        String PASSWORD_RETRY_COUNT_KEY = PASSWORD_RETRY_PREFIX + "%s";

        /**
         * 密码锁定状态缓存Key格式: auth:password:lock:{username}
         */
        String PASSWORD_LOCK_KEY = "auth:password:lock:%s";

        /**
         * 登录频率限制前缀
         */
        String LOGIN_FREQUENCY_PREFIX = "auth:login:frequency:";

        /**
         * 每日登录失败次数Key格式: auth:login:frequency:fail:day:{username}
         */
        String LOGIN_FAIL_DAY_KEY = LOGIN_FREQUENCY_PREFIX + "fail:day:%s";

        /**
         * 每小时登录失败次数Key格式: auth:login:frequency:fail:hour:{username}
         */
        String LOGIN_FAIL_HOUR_KEY = LOGIN_FREQUENCY_PREFIX + "fail:hour:%s";

        /**
         * 每日登录成功次数Key格式: auth:login:frequency:success:day:{username}
         */
        String LOGIN_SUCCESS_DAY_KEY = LOGIN_FREQUENCY_PREFIX + "success:day:%s";

        /**
         * 每小时登录成功次数Key格式: auth:login:frequency:success:hour:{username}
         */
        String LOGIN_SUCCESS_HOUR_KEY = LOGIN_FREQUENCY_PREFIX + "success:hour:%s";

    }


    /**
     * 商城商品缓存相关常量
     */
    interface MallProduct {
        /**
         * 缓存名称
         */
        String CACHE_NAME = "mall:product:detail";

        /**
         * 缓存 Key 前缀
         */
        String KEY_PREFIX = "mall:product:detail:";

        /**
         * Key 模板 mall:product:detail:{productId}
         */
        String DETAIL_KEY = KEY_PREFIX + "%s";

        /**
         * 缓存有效期（秒）- 30分钟
         */
        long CACHE_TTL_SECONDS = 30 * 60;
    }

    /**
     * 商品索引批量同步相关缓存
     */
    interface MallProductIndex {
        /**
         * 上次批量索引的游标 key
         */
        String INDEX_CURSOR_KEY = "mall:product:index:cursor";

        /**
         * 商品销量增量计数 key（每满固定阈值触发 ES 刷新）
         */
        String SALES_SYNC_COUNTER_KEY = "mall:product:index:sales:delta:%s";
    }

    /**
     * Agent 配置缓存相关常量
     */
    interface AgentConfig {
        /**
         * Agent 全量配置缓存 key
         */
        String ALL_CONFIG_KEY = "agent:config:all";
    }

}
