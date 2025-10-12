package cn.zhangchuangla.medicine.admin.common.core.constants;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 14:32
 */
public class Constants {


    /**
     * JSON 序列化白名单
     */
    public static final String[] JSON_WHITELIST_STR = {"org.springframework", "cn.zhangchuangla.medicine"};

    /**
     * 账号状态常量
     */
    public static final Integer ACCOUNT_UNLOCK_KEY = 0;


    /**
     * 已删除状态常量
     */
    public static final int DELETED = 1;

    /**
     * 未删除状态常量
     */
    public static final int NOT_DELETED = 0;

    public static final String STATIC_FILE = "files";

}
