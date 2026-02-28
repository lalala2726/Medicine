package cn.zhangchuangla.medicine.common.ip.utils;

import cn.zhangchuangla.medicine.common.ip.config.IP2RegionConfig;
import cn.zhangchuangla.medicine.common.ip.entity.IPEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IPUtils} 单元测试。
 */
class IPUtilsTests {

    /**
     * 功能描述：每个测试开始前重置静态搜索器，避免测试之间互相污染。
     *
     * @param 无参数。
     * @return 无返回值。
     * @throws 无显式异常。
     */
    @BeforeEach
    void setUp() {
        IPUtils.resetSearcherForTest();
    }

    /**
     * 功能描述：每个测试结束后重置静态搜索器，防止影响后续模块测试。
     *
     * @param 无参数。
     * @return 无返回值。
     * @throws 无显式异常。
     */
    @AfterEach
    void tearDown() {
        IPUtils.resetSearcherForTest();
    }

    /**
     * 测试目的：验证多级代理头场景下会选择第一个有效 IP。
     * 预期结果：返回 `X-Forwarded-For` 中第一个合法且非 unknown 的地址。
     */
    @Test
    void shouldReturnFirstValidIpFromForwardedHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown, 198.51.100.7, 10.0.0.1");
        request.setRemoteAddr("203.0.113.20");

        String actualIp = IPUtils.getIpAddress(request);

        assertEquals("198.51.100.7", actualIp);
    }

    /**
     * 测试目的：验证没有有效代理头时会回退到 remoteAddr。
     * 预期结果：返回 request.remoteAddr 的值。
     */
    @Test
    void shouldFallbackToRemoteAddressWhenHeadersAreInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("Proxy-Client-IP", "localhost");
        request.setRemoteAddr("203.0.113.9");

        String actualIp = IPUtils.getIpAddress(request);

        assertEquals("203.0.113.9", actualIp);
    }

    /**
     * 测试目的：验证局域网 IPv4 地址会被识别为私网标签。
     * 预期结果：country 字段为“局域网”。
     */
    @Test
    void shouldMarkPrivateIpAsLan() {
        IPEntity entity = IPUtils.getRegionEntity("192.168.1.10");
        assertEquals("局域网", entity.getCountry());
    }

    /**
     * 测试目的：验证回环 IPv4 地址会被识别为本机标签。
     * 预期结果：country 字段为“本机”。
     */
    @Test
    void shouldMarkLoopbackIpAsLocalhost() {
        IPEntity entity = IPUtils.getRegionEntity("127.0.0.1");
        assertEquals("本机", entity.getCountry());
    }

    /**
     * 测试目的：验证运营商 CGN 地址段会被识别为运营商 NAT 标签。
     * 预期结果：country 字段为“运营商NAT”。
     */
    @Test
    void shouldMarkCgnIpAsCarrierNat() {
        IPEntity entity = IPUtils.getRegionEntity("100.64.12.4");
        assertEquals("运营商NAT", entity.getCountry());
    }

    /**
     * 测试目的：验证 IPv6 地址不参与 ip2region 查询并返回固定提示。
     * 预期结果：country/area/region/isp 均为“IPv6不支持查询”。
     */
    @Test
    void shouldReturnIpv6NotSupportedForIpv6Address() {
        IPEntity entity = IPUtils.getRegionEntity("2409:8a20:6d:4f80::1");
        assertEquals("IPv6不支持查询", entity.getCountry());
        assertEquals("IPv6不支持查询", entity.getArea());
        assertEquals("IPv6不支持查询", entity.getRegion());
        assertEquals("IPv6不支持查询", entity.getISP());
    }

    /**
     * 测试目的：验证配置关闭时不会初始化搜索器并对公网 IP 降级返回“未知”。
     * 预期结果：搜索器模式为 DISABLED，公网 IP 查询结果为“未知”。
     */
    @Test
    void shouldDowngradeToUnknownWhenIp2RegionDisabled() {
        IPUtils ipUtils = buildIpUtils(false, true, "classpath:data/ip2region.xdb");
        ipUtils.init();

        assertEquals("DISABLED", IPUtils.getSearcherModeForTest());
        assertEquals("未知", IPUtils.getRegion("8.8.8.8"));
    }

    /**
     * 测试目的：验证 cache-enabled=true 时使用内存缓存模式初始化。
     * 预期结果：搜索器模式为 BUFFER，且搜索器状态非空可用。
     */
    @Test
    void shouldInitializeSearcherInBufferModeWhenCacheEnabled() {
        IPUtils ipUtils = buildIpUtils(true, true, "classpath:data/ip2region.xdb");
        ipUtils.init();

        assertEquals("BUFFER", IPUtils.getSearcherModeForTest());
        IPEntity entity = IPUtils.getRegionEntity("8.8.8.8");
        assertNotNull(entity);
    }

    /**
     * 测试目的：验证 cache-enabled=false 时使用文件模式初始化。
     * 预期结果：搜索器模式为 FILE，且 IPv6 逻辑仍保持可用。
     */
    @Test
    void shouldInitializeSearcherInFileModeWhenCacheDisabled() {
        IPUtils ipUtils = buildIpUtils(true, false, "classpath:data/ip2region.xdb");
        ipUtils.init();

        assertEquals("FILE", IPUtils.getSearcherModeForTest());
        assertTrue(IPUtils.isIPv6("2409:8a20:6d:4f80::1"));
    }

    /**
     * 功能描述：创建 IPUtils 测试实例并注入指定配置。
     *
     * @param enabled {@code boolean} 是否启用 ip2region，默认 true。
     * @param cacheEnabled {@code boolean} 是否使用内存缓存，默认 true。
     * @param dbPath {@link String} 数据库路径，支持 classpath: 与本地路径。
     * @return {@link IPUtils} 绑定指定配置的工具实例。
     * @throws 无显式异常。
     */
    private IPUtils buildIpUtils(boolean enabled, boolean cacheEnabled, String dbPath) {
        IP2RegionConfig config = new IP2RegionConfig();
        config.setEnabled(enabled);
        config.setCacheEnabled(cacheEnabled);
        config.setDbPath(dbPath);
        return new IPUtils(config, new DefaultResourceLoader());
    }
}
