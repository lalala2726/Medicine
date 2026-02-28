package cn.zhangchuangla.medicine.common.ip.utils;

import cn.zhangchuangla.medicine.common.ip.config.IP2RegionConfig;
import cn.zhangchuangla.medicine.common.ip.entity.IPEntity;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * IP 工具类，负责解析客户端 IP 并查询归属地信息。
 */
@Component
public class IPUtils {

    private static final Logger log = LoggerFactory.getLogger(IPUtils.class);

    private static final String DEFAULT_DB_CLASSPATH_PATH = "classpath:data/ip2region.xdb";
    private static final String UNKNOWN_IP_INFO = "未知";
    private static final String PRIVATE_IP_INFO = "局域网";
    private static final String LOCALHOST_IO_INFO = "本机";
    private static final String CGN_IP_INFO = "运营商NAT";
    private static final String IPV6_NOT_SUPPORT_INFO = "IPv6不支持查询";

    private static final String MODE_UNINITIALIZED = "UNINITIALIZED";
    private static final String MODE_DISABLED = "DISABLED";
    private static final String MODE_FILE = "FILE";
    private static final String MODE_BUFFER = "BUFFER";
    private static final String MODE_FAILED = "FAILED";

    private static final String[] CANDIDATE_IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private static volatile Searcher searcher;
    private static volatile String searcherMode = MODE_UNINITIALIZED;

    private final IP2RegionConfig ip2RegionConfig;
    private final ResourceLoader resourceLoader;

    /**
     * 构造 IP 工具组件。
     *
     * @param ip2RegionConfig {@link IP2RegionConfig} IP2Region 配置对象，默认值来自 application 配置。
     * @param resourceLoader {@link ResourceLoader} Spring 资源加载器，用于读取 classpath 与文件系统资源。
     * @return 无返回值。
     * @throws NullPointerException 当 Spring 注入失败导致任一参数为 null 时抛出。
     */
    public IPUtils(IP2RegionConfig ip2RegionConfig, ResourceLoader resourceLoader) {
        this.ip2RegionConfig = ip2RegionConfig;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 获取客户端真实 IP 地址。
     *
     * @param request {@link HttpServletRequest} HTTP 请求对象，允许为 null，默认返回空字符串。
     * @return {@link String} 客户端 IP 字符串；解析失败或 request 为空时返回空字符串。
     * @throws 无显式抛出；内部异常会被捕获并记录日志。
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        try {
            for (String headerName : CANDIDATE_IP_HEADERS) {
                String candidateIp = normalizeForwardedIp(request.getHeader(headerName));
                if (isValidIp(candidateIp)) {
                    return candidateIp;
                }
            }
            return normalizeForwardedIp(request.getRemoteAddr());
        } catch (Exception exception) {
            log.error("IPUtils getIpAddress error", exception);
            return "";
        }
    }

    /**
     * 判断输入 IP 是否为 IPv6 地址。
     *
     * @param ip {@link String} IP 字符串，允许为空。
     * @return {@code boolean} 为 IPv6 返回 true，否则返回 false。
     * @throws 无显式抛出。
     */
    public static boolean isIPv6(String ip) {
        return StringUtils.isNotBlank(ip) && ip.contains(":") && !ip.contains(".");
    }

    /**
     * 根据 IP 返回可展示的归属地文本。
     *
     * @param ip {@link String} IP 字符串，默认仅支持 IPv4 归属地库查询。
     * @return {@link String} 归属地文本；IPv6 返回“IPv6不支持查询”，无法识别返回“未知”。
     * @throws 无显式抛出；内部异常会降级为“未知”。
     */
    public static String getRegion(String ip) {
        if (isIPv6(ip)) {
            return IPV6_NOT_SUPPORT_INFO;
        }

        IPEntity regionEntity = getRegionEntity(ip);
        StringBuilder regionBuilder = new StringBuilder();
        appendRegionPart(regionBuilder, regionEntity.getCountry());
        appendRegionPart(regionBuilder, regionEntity.getArea());
        appendRegionPart(regionBuilder, regionEntity.getRegion());
        appendRegionPart(regionBuilder, regionEntity.getISP());
        return regionBuilder.isEmpty() ? UNKNOWN_IP_INFO : regionBuilder.toString();
    }

    /**
     * 根据 IP 返回结构化归属地对象。
     *
     * @param ip {@link String} IP 字符串；为空时仅返回默认未知字段。
     * @return {@link IPEntity} 结构化归属地对象，包含国家、省份、地区与运营商信息。
     * @throws 无显式抛出；查询失败会降级为“未知”并返回可用对象。
     */
    public static IPEntity getRegionEntity(String ip) {
        IPEntity ipEntity = new IPEntity();
        ipEntity.setIp(ip);

        if (StringUtils.isBlank(ip)) {
            ipEntity.setRegion(UNKNOWN_IP_INFO);
            ipEntity.setISP(UNKNOWN_IP_INFO);
            return ipEntity;
        }

        if (isIPv6(ip)) {
            ipEntity.setCountry(IPV6_NOT_SUPPORT_INFO);
            ipEntity.setArea(IPV6_NOT_SUPPORT_INFO);
            ipEntity.setRegion(IPV6_NOT_SUPPORT_INFO);
            ipEntity.setISP(IPV6_NOT_SUPPORT_INFO);
            return ipEntity;
        }

        if (isPrivateOrCgnIp(ip)) {
            ipEntity.setCountry(resolveIntranetTag(ip));
            return ipEntity;
        }

        String regionResult = getRegionString(ip);
        if (regionResult == null) {
            ipEntity.setRegion(UNKNOWN_IP_INFO);
            ipEntity.setISP(UNKNOWN_IP_INFO);
            return ipEntity;
        }

        String[] parts = regionResult.split("\\|");
        if (parts.length < 5) {
            log.warn("Unexpected ip2region result format for IP {}: {}", ip, regionResult);
            ipEntity.setRegion(regionResult);
            ipEntity.setISP(UNKNOWN_IP_INFO);
            return ipEntity;
        }

        ipEntity.setCountry("0".equals(parts[0]) ? UNKNOWN_IP_INFO : parts[0]);
        String province = "0".equals(parts[2]) ? "" : parts[2];
        String city = "0".equals(parts[3]) ? "" : parts[3];
        ipEntity.setArea(province.isEmpty() ? UNKNOWN_IP_INFO : province);
        ipEntity.setRegion(buildRegionDetail(province, city));
        ipEntity.setISP("0".equals(parts[4]) ? UNKNOWN_IP_INFO : parts[4]);
        return ipEntity;
    }

    /**
     * 供测试读取当前搜索器初始化模式。
     *
     * @param 无参数。
     * @return {@link String} 当前初始化模式，可能值为 UNINITIALIZED、DISABLED、FILE、BUFFER、FAILED。
     * @throws 无显式抛出。
     */
    static String getSearcherModeForTest() {
        return searcherMode;
    }

    /**
     * 供测试重置静态搜索器状态，避免用例间互相污染。
     *
     * @param 无参数。
     * @return 无返回值。
     * @throws 无显式抛出；关闭旧搜索器失败会被吞并。
     */
    static void resetSearcherForTest() {
        synchronized (IPUtils.class) {
            closeSearcherQuietly(searcher);
            searcher = null;
            searcherMode = MODE_UNINITIALIZED;
        }
    }

    /**
     * 查询 ip2region 原始结果。
     *
     * @param ip {@link String} IPv4 地址字符串。
     * @return {@link String} 原始归属地结果字符串；不可用或查询失败返回 null。
     * @throws 无显式抛出；内部异常会记录日志后返回 null。
     */
    private static String getRegionString(String ip) {
        Searcher currentSearcher = searcher;
        if (currentSearcher == null) {
            if (!MODE_DISABLED.equals(searcherMode)) {
                log.warn("Ip2region searcher is not available, mode={}", searcherMode);
            }
            return null;
        }
        if (StringUtils.isBlank(ip)) {
            return null;
        }
        try {
            String region = currentSearcher.search(ip);
            return StringUtils.isBlank(region) ? null : region;
        } catch (Exception exception) {
            log.error("Ip2region search error for IP: {}", ip, exception);
            return null;
        }
    }

    /**
     * 解析并归一化代理链中的 IP。
     *
     * @param ip {@link String} 可能包含逗号分隔代理链的 IP 文本。
     * @return {@link String} 第一个有效 IP；都无效时返回首个分段或空字符串。
     * @throws 无显式抛出。
     */
    private static String normalizeForwardedIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return "";
        }
        if (!ip.contains(",")) {
            return ip.trim();
        }

        String[] ips = ip.split(",");
        for (String singleIp : ips) {
            String candidateIp = singleIp.trim();
            if (isValidIp(candidateIp)) {
                return candidateIp;
            }
        }
        return ips.length == 0 ? "" : ips[0].trim();
    }

    /**
     * 判断 IP 值是否有效。
     *
     * @param ip {@link String} IP 字符串，支持 IPv4/IPv6 文本格式。
     * @return {@code boolean} 符合基础格式且不是明显无效占位值时返回 true。
     * @throws 无显式抛出。
     */
    private static boolean isValidIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        String normalizedIp = ip.trim().toLowerCase();
        String[] invalidValues = {"unknown", "null", "undefined", "localhost", "0:0:0:0:0:0:0:1", "::"};
        for (String invalidValue : invalidValues) {
            if (invalidValue.equals(normalizedIp)) {
                return false;
            }
        }
        if (isValidIPv4(normalizedIp)) {
            return !"0.0.0.0".equals(normalizedIp) && !"127.0.0.1".equals(normalizedIp);
        }
        return isValidIPv6(normalizedIp);
    }

    /**
     * 校验 IPv4 地址格式合法性。
     *
     * @param ip {@link String} IPv4 字符串。
     * @return {@code boolean} 四段且每段为 0-255 且无非法前导零时返回 true。
     * @throws 无显式抛出。
     */
    private static boolean isValidIPv4(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            return false;
        }
        try {
            for (String octet : octets) {
                int value = Integer.parseInt(octet);
                if (value < 0 || value > 255) {
                    return false;
                }
                if (octet.length() > 1 && octet.startsWith("0")) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    /**
     * 校验 IPv6 地址格式合法性（轻量判定）。
     *
     * @param ip {@link String} IPv6 字符串。
     * @return {@code boolean} 形态满足基础特征时返回 true。
     * @throws 无显式抛出。
     */
    private static boolean isValidIPv6(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        return ip.contains(":")
                && ip.length() >= 2
                && ip.length() <= 39
                && !"::".equals(ip)
                && !"0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * 判断是否为私网、回环或运营商 NAT 地址。
     *
     * @param ip {@link String} IPv4 字符串。
     * @return {@code boolean} 命中内网/回环/CGN 段返回 true。
     * @throws 无显式抛出；格式异常时返回 false。
     */
    private static boolean isPrivateOrCgnIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        try {
            String[] octets = ip.split("\\.");
            if (octets.length != 4) {
                return false;
            }
            int firstOctet = Integer.parseInt(octets[0]);
            int secondOctet = Integer.parseInt(octets[1]);
            if (firstOctet == 10) {
                return true;
            }
            if (firstOctet == 172 && (secondOctet >= 16 && secondOctet <= 31)) {
                return true;
            }
            if (firstOctet == 192 && secondOctet == 168) {
                return true;
            }
            if (firstOctet == 100 && (secondOctet >= 64 && secondOctet <= 127)) {
                return true;
            }
            return firstOctet == 127;
        } catch (NumberFormatException exception) {
            log.warn("Invalid IP format for private range check: {}", ip);
            return false;
        }
    }

    /**
     * 解析私网类地址标签。
     *
     * @param ip {@link String} 命中私网规则的 IPv4 字符串。
     * @return {@link String} 回环返回“本机”、CGN 返回“运营商NAT”、其他私网返回“局域网”。
     * @throws 无显式抛出。
     */
    private static String resolveIntranetTag(String ip) {
        if (ip.startsWith("127.")) {
            return LOCALHOST_IO_INFO;
        }
        if (ip.startsWith("100.") && ip.split("\\.").length == 4) {
            int secondOctet = Integer.parseInt(ip.split("\\.")[1]);
            if (secondOctet >= 64 && secondOctet <= 127) {
                return CGN_IP_INFO;
            }
        }
        return PRIVATE_IP_INFO;
    }

    /**
     * 构造省市合并后的详细地区文本。
     *
     * @param province {@link String} 省份文本，默认空字符串表示未知。
     * @param city {@link String} 城市文本，默认空字符串表示未知。
     * @return {@link String} 拼接后的地区字符串；无有效信息时返回“未知”。
     * @throws 无显式抛出。
     */
    private static String buildRegionDetail(String province, String city) {
        StringBuilder regionBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(province)) {
            regionBuilder.append(province);
        }
        if (StringUtils.isNotBlank(city) && !StringUtils.equals(province, city)) {
            if (!regionBuilder.isEmpty()) {
                regionBuilder.append(" ");
            }
            regionBuilder.append(city);
        }
        return regionBuilder.isEmpty() ? UNKNOWN_IP_INFO : regionBuilder.toString();
    }

    /**
     * 向地区文本中按需追加片段。
     *
     * @param regionBuilder {@link StringBuilder} 结果拼接器，默认由调用方初始化为空。
     * @param value {@link String} 候选片段，为空或“未知”时会被忽略。
     * @return 无返回值。
     * @throws NullPointerException 当 regionBuilder 为 null 时抛出。
     */
    private static void appendRegionPart(StringBuilder regionBuilder, String value) {
        if (StringUtils.isBlank(value) || UNKNOWN_IP_INFO.equals(value)) {
            return;
        }
        if (!regionBuilder.isEmpty()) {
            regionBuilder.append(" ");
        }
        regionBuilder.append(value);
    }

    /**
     * 原子替换当前搜索器实例并关闭旧实例。
     *
     * @param newSearcher {@link Searcher} 新构建的搜索器实例。
     * @param mode {@link String} 新实例模式，取值 FILE 或 BUFFER。
     * @return 无返回值。
     * @throws NullPointerException 当 newSearcher 或 mode 为 null 时抛出。
     */
    private static void replaceSearcher(Searcher newSearcher, String mode) {
        closeSearcherQuietly(searcher);
        searcher = newSearcher;
        searcherMode = mode;
    }

    /**
     * 安静关闭搜索器，避免影响业务流程。
     *
     * @param targetSearcher {@link Searcher} 需要关闭的搜索器实例，允许为 null。
     * @return 无返回值。
     * @throws 无显式抛出；关闭失败仅记录 debug 日志。
     */
    private static void closeSearcherQuietly(Searcher targetSearcher) {
        if (targetSearcher == null) {
            return;
        }
        try {
            targetSearcher.close();
        } catch (Exception exception) {
            log.debug("Failed to close ip2region searcher quietly", exception);
        }
    }

    /**
     * 应用启动后初始化 ip2region 搜索器。
     *
     * @param 无参数。
     * @return 无返回值。
     * @throws 无显式抛出；初始化失败会记录日志并自动降级为不可用状态。
     */
    @PostConstruct
    public void init() {
        synchronized (IPUtils.class) {
            if (!ip2RegionConfig.isEnabled()) {
                closeSearcherQuietly(searcher);
                searcher = null;
                searcherMode = MODE_DISABLED;
                log.info("IP region lookup is disabled by configuration: ip2region.enabled=false");
                return;
            }

            String configuredPath = StringUtils.defaultIfBlank(ip2RegionConfig.getDbPath(), DEFAULT_DB_CLASSPATH_PATH);
            try {
                Searcher initializedSearcher;
                if (ip2RegionConfig.isCacheEnabled()) {
                    byte[] dbBytes = loadDbBytes(configuredPath);
                    initializedSearcher = Searcher.newWithBuffer(dbBytes);
                    replaceSearcher(initializedSearcher, MODE_BUFFER);
                } else {
                    String filePath = resolveDbFilePath(configuredPath);
                    initializedSearcher = Searcher.newWithFileOnly(filePath);
                    replaceSearcher(initializedSearcher, MODE_FILE);
                }
                log.info("Ip2region searcher initialized successfully. dbPath={}, cacheEnabled={}, mode={}",
                        configuredPath, ip2RegionConfig.isCacheEnabled(), searcherMode);
            } catch (Exception exception) {
                closeSearcherQuietly(searcher);
                searcher = null;
                searcherMode = MODE_FAILED;
                log.error("Ip2region searcher initialization failed. dbPath={}, cacheEnabled={}",
                        configuredPath, ip2RegionConfig.isCacheEnabled(), exception);
            }
        }
    }

    /**
     * 读取归属地数据库字节内容。
     *
     * @param dbPath {@link String} 数据库路径，支持 classpath: 与文件路径。
     * @return {@code byte[]} 数据库完整字节数组。
     * @throws IOException 当资源不存在或读取失败时抛出。
     */
    private byte[] loadDbBytes(String dbPath) throws IOException {
        if (isClasspathPath(dbPath)) {
            try (InputStream inputStream = openClasspathInputStream(dbPath)) {
                return inputStream.readAllBytes();
            }
        }
        Path filePath = Paths.get(dbPath);
        if (!Files.exists(filePath)) {
            throw new IOException("ip2region db file not found: " + dbPath);
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * 将配置路径解析为可供 `Searcher.newWithFileOnly` 使用的本地文件路径。
     *
     * @param dbPath {@link String} 数据库路径，支持 classpath: 与文件路径。
     * @return {@link String} 本地可读文件绝对路径。
     * @throws IOException 当资源不存在或临时文件写入失败时抛出。
     */
    private String resolveDbFilePath(String dbPath) throws IOException {
        if (!isClasspathPath(dbPath)) {
            Path configuredPath = Paths.get(dbPath);
            if (!Files.exists(configuredPath)) {
                throw new IOException("ip2region db file not found: " + dbPath);
            }
            return configuredPath.toAbsolutePath().toString();
        }

        try (InputStream inputStream = openClasspathInputStream(dbPath)) {
            Path tempDbPath = Files.createTempFile("ip2region_", ".xdb");
            tempDbPath.toFile().deleteOnExit();
            Files.copy(inputStream, tempDbPath, StandardCopyOption.REPLACE_EXISTING);
            return tempDbPath.toAbsolutePath().toString();
        }
    }

    /**
     * 按 classpath 路径打开资源输入流。
     *
     * @param dbPath {@link String} classpath 资源路径，格式如 `classpath:data/ip2region.xdb`。
     * @return {@link InputStream} 资源输入流，调用方负责关闭。
     * @throws IOException 当资源不存在或无法打开时抛出。
     */
    private InputStream openClasspathInputStream(String dbPath) throws IOException {
        Resource resource = resourceLoader.getResource(dbPath);
        if (!resource.exists()) {
            throw new IOException("ip2region classpath resource not found: " + dbPath);
        }
        return resource.getInputStream();
    }

    /**
     * 判断路径是否为 classpath 资源。
     *
     * @param dbPath {@link String} 数据库路径字符串，允许为空。
     * @return {@code boolean} 以 `classpath:` 开头返回 true，否则返回 false。
     * @throws 无显式抛出。
     */
    private boolean isClasspathPath(String dbPath) {
        return dbPath != null && dbPath.startsWith("classpath:");
    }
}
