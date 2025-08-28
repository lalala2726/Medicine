package cn.zhangchuangla.medicine.common.utils;

import java.util.UUID;

/**
 * @author Chuang
 */
public class UUIDUtils {

    /**
     * 获取一个简单UUID
     *
     * @return 简单UUID
     */
    public static String simple() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
