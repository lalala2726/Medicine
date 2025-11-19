package cn.zhangchuangla.medicine.common.mongodb.constants;

/**
 * MongoDB集合名称常量类
 * 定义系统中使用的所有MongoDB集合名称
 *
 * @author Chuang
 */
public final class MongoCollections {

    /**
     * 地址区域集合
     * 存储全国省市区街道信息
     */
    public static final String REGIONS = "regions";
    /**
     * AI对话集合
     * 存储用户与AI的对话会话信息
     */
    public static final String AI_CONVERSATIONS = "ai_conversations";
    /**
     * AI消息集合
     * 存储AI对话中的具体消息内容
     */
    public static final String AI_MESSAGES = "ai_messages";
    /**
     * 用户行为日志集合
     * 存储用户操作行为日志
     */
    public static final String USER_BEHAVIOR_LOGS = "user_behavior_logs";
    /**
     * 系统配置集合
     * 存储系统动态配置信息
     */
    public static final String SYSTEM_CONFIGS = "system_configs";

    private MongoCollections() {
        // 工具类,禁止实例化
    }
}

