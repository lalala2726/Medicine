package cn.zhangchuangla.medicine.processing.parser;

public final class ParserUtils {

    private ParserUtils() {
    }

    /**
     * 统一换行并去除 BOM/首尾空白。
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace("\r\n", "\n").trim();
        if (normalized.startsWith("\uFEFF")) {
            normalized = normalized.substring(1).trim();
        }
        return normalized;
    }

    /**
     * 判断文本是否含有效字符。
     */
    public static boolean hasText(String text) {
        return !normalize(text).isEmpty();
    }
}

