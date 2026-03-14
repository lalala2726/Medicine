package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 管理端智能体用户工具控制器单元测试类。
 * <p>
 * 测试目标：验证 {@link AgentUserController} 的用户查询功能是否正确委托给 Service 层
 * 测试接口：
 * <ul>
 *     <li>GET /agent/admin/user/info - 获取当前用户信息</li>
 *     <li>GET /agent/admin/user/list - 用户列表</li>
 *     <li>GET /agent/admin/user/{userId}/detail - 用户详情</li>
 *     <li>GET /agent/admin/user/{userId}/wallet - 用户钱包</li>
 *     <li>GET /agent/admin/user/{userId}/wallet_flow - 用户钱包流水</li>
 *     <li>GET /agent/admin/user/{userId}/consume_info - 用户消费信息</li>
 * </ul>
 *
 * @author Chuang
 */
class AgentUserControllerTests {

    private final StubUserService userService = new StubUserService();
    private final AgentUserController controller = new AgentUserController(userService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    /**
     * 测试用户列表查询是否正确委托给 Service。
     * <p>
     * 测试目的：验证 Controller 正确调用 Service 层的 listUsers 方法，
     * 并将查询参数传递给 Service。
     * 测试接口：GET /agent/admin/user/list
     * 预期结果：返回状态码 200，Service 接收到正确的请求参数
     */
    @Test
    void listUsers_ShouldDelegateToService() {
        UserListQueryRequest request = new UserListQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setNickname("张三");
        userService.userPage = createSampleUserPage();

        var result = controller.listUsers(request);

        assertEquals(200, result.getCode());
        assertTrue(userService.listUsersInvoked);
        assertEquals(request, userService.capturedListRequest);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listUsers_ShouldSupportMapRecordsFromRpc() {
        UserListQueryRequest request = new UserListQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        Page<UserListDto> page = new Page<>(1, 10);
        page.setTotal(1);
        Map<String, Object> rawRow = new LinkedHashMap<>();
        rawRow.put("id", 11L);
        rawRow.put("username", "rpc_user");
        rawRow.put("nickname", "RPC用户");
        rawRow.put("roles", "user");
        rawRow.put("status", 1);
        rawRow.put("createTime", new Date());
        ((Page) page).setRecords(List.of(rawRow));
        userService.userPage = page;

        var result = controller.listUsers(request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getRows());
        assertEquals(1, result.getData().getRows().size());
        assertNotNull(result.getData().getRows().get(0));
    }

    /**
     * 测试用户详情查询是否正确委托给 Service。
     * <p>
     * 测试目的：验证 Controller 正确调用 Service 层的 getUserDetailById 方法，
     * 并将用户 ID 传递给 Service。
     * 测试接口：GET /agent/admin/user/{userId}/detail
     * 预期结果：返回状态码 200，包含用户详情信息
     */
    @Test
    void getUserDetail_ShouldDelegateToService() {
        Long userId = 1L;
        userService.userDetail = createSampleUserDetail();

        var result = controller.getUserDetail(userId);

        assertEquals(200, result.getCode());
        assertTrue(userService.getUserDetailInvoked);
        assertEquals(userId, userService.capturedUserId);
        assertNotNull(result.getData());
        assertEquals("张三", result.getData().getNickName());
    }

    /**
     * 测试用户钱包查询是否正确委托给 Service。
     * <p>
     * 测试目的：验证 Controller 正确调用 Service 层的 getUserWalletByUserId 方法，
     * 并将用户 ID 传递给 Service。
     * 测试接口：GET /agent/admin/user/{userId}/wallet
     * 预期结果：返回状态码 200，包含用户钱包信息
     */
    @Test
    void getUserWallet_ShouldDelegateToService() {
        Long userId = 1L;
        userService.userWallet = createSampleUserWallet();

        var result = controller.getUserWallet(userId);

        assertEquals(200, result.getCode());
        assertTrue(userService.getUserWalletInvoked);
        assertEquals(userId, userService.capturedWalletUserId);
        assertNotNull(result.getData());
        assertEquals(new BigDecimal("1000.00"), result.getData().getBalance());
    }

    /**
     * 测试钱包流水查询是否正确委托给 Service。
     * <p>
     * 测试目的：验证 Controller 正确调用 Service 层的 getUserWalletFlow 方法，
     * 并将用户 ID 和分页参数传递给 Service。
     * 测试接口：GET /agent/admin/user/{userId}/wallet_flow
     * 预期结果：返回状态码 200，包含钱包流水分页数据
     */
    @Test
    void getUserWalletFlow_ShouldDelegateToService() {
        Long userId = 1L;
        PageRequest request = new PageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        userService.walletFlowResult = createSampleWalletFlowResult();

        var result = controller.getUserWalletFlow(userId, request);

        assertEquals(200, result.getCode());
        assertTrue(userService.getUserWalletFlowInvoked);
        assertEquals(userId, userService.capturedFlowUserId);
        assertEquals(request, userService.capturedFlowPageRequest);
    }

    /**
     * 测试消费信息查询是否正确委托给 Service。
     * <p>
     * 测试目的：验证 Controller 正确调用 Service 层的 getConsumeInfo 方法，
     * 并将用户 ID 和分页参数传递给 Service。
     * 测试接口：GET /agent/admin/user/{userId}/consume_info
     * 预期结果：返回状态码 200，包含消费信息分页数据
     */
    @Test
    void getConsumeInfo_ShouldDelegateToService() {
        Long userId = 1L;
        PageRequest request = new PageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        userService.consumeInfoResult = createSampleConsumeInfoResult();

        var result = controller.getConsumeInfo(userId, request);

        assertEquals(200, result.getCode());
        assertTrue(userService.getConsumeInfoInvoked);
        assertEquals(userId, userService.capturedConsumeUserId);
        assertEquals(request, userService.capturedConsumePageRequest);
    }

    // ==================== Helper Methods ====================

    private void setupAuthentication(AuthUser authUser, Set<String> authorities) {
        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toSet()));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    private AuthUser sampleAuthUser() {
        Date now = new Date();
        return AuthUser.builder()
                .id(7L)
                .username("agent_user")
                .status(0)
                .nickname("智能体用户")
                .avatar("https://example.com/avatar.png")
                .email("agent@example.com")
                .phoneNumber("13800000000")
                .gender(1)
                .birthday(now)
                .realName("智能体")
                .idCard("310101199001010011")
                .lastLoginTime(now)
                .lastLoginIp("127.0.0.1")
                .createTime(now)
                .updateTime(now)
                .createBy("system")
                .updateBy("system")
                .isDelete(0)
                .build();
    }

    private Page<UserListDto> createSampleUserPage() {
        Page<UserListDto> page = new Page<>(1, 10);
        page.setTotal(1);

        UserListDto userDto = new UserListDto();
        userDto.setId(1L);
        userDto.setUsername("zhangsan");
        userDto.setNickname("张三");
        userDto.setAvatar("https://example.com/avatar.jpg");
        userDto.setRoles("user");
        userDto.setStatus(1);
        userDto.setCreateTime(new Date());

        page.setRecords(List.of(userDto));
        return page;
    }

    private UserDetailDto createSampleUserDetail() {
        UserDetailDto detail = new UserDetailDto();
        detail.setAvatar("https://example.com/avatar.jpg");
        detail.setNickName("张三");
        detail.setWalletBalance(new BigDecimal("1000.00"));
        detail.setTotalOrders(10);
        detail.setTotalConsume(new BigDecimal("5000.00"));

        UserDetailDto.BasicInfo basicInfo = new UserDetailDto.BasicInfo();
        basicInfo.setUserId(1L);
        basicInfo.setRealName("张三");
        basicInfo.setPhoneNumber("13800000000");
        basicInfo.setEmail("zhangsan@example.com");
        basicInfo.setGender(1);
        detail.setBasicInfo(basicInfo);

        UserDetailDto.SecurityInfo securityInfo = new UserDetailDto.SecurityInfo();
        securityInfo.setRegisterTime(new Date());
        securityInfo.setLastLoginTime(new Date());
        securityInfo.setLastLoginIp("127.0.0.1");
        securityInfo.setStatus(0);
        detail.setSecurityInfo(securityInfo);

        return detail;
    }

    private UserWalletDto createSampleUserWallet() {
        UserWalletDto wallet = new UserWalletDto();
        wallet.setUserId(1L);
        wallet.setWalletNo("W202501010001");
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setTotalIncome(new BigDecimal("5000.00"));
        wallet.setTotalExpend(new BigDecimal("4000.00"));
        wallet.setCurrency("CNY");
        wallet.setStatus(1);
        wallet.setUpdatedAt(new Date());
        return wallet;
    }

    private Page<UserWalletFlowDto> createSampleWalletFlowResult() {
        UserWalletFlowDto flow = new UserWalletFlowDto();
        flow.setIndex(1L);
        flow.setChangeType("订单支付");
        flow.setAmount(new BigDecimal("100.00"));
        flow.setAmountDirection(2);
        flow.setIsIncome(false);
        flow.setBeforeBalance(new BigDecimal("1100.00"));
        flow.setAfterBalance(new BigDecimal("1000.00"));
        flow.setChangeTime(new Date());

        Page<UserWalletFlowDto> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(flow));
        return page;
    }

    private Page<UserConsumeInfoDto> createSampleConsumeInfoResult() {
        UserConsumeInfoDto consume = new UserConsumeInfoDto();
        consume.setIndex(1L);
        consume.setUserId(1L);
        consume.setOrderNo("O202510312122");
        consume.setTotalPrice(new BigDecimal("100.00"));
        consume.setPayPrice(new BigDecimal("95.00"));
        consume.setFinishTime(new Date());

        Page<UserConsumeInfoDto> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(consume));
        return page;
    }

    // ==================== Stub Service ====================

    private static class StubUserService implements UserService {

        private Page<UserListDto> userPage = new Page<>();
        private UserDetailDto userDetail;
        private UserWalletDto userWallet;
        private Page<UserWalletFlowDto> walletFlowResult = new Page<>();
        private Page<UserConsumeInfoDto> consumeInfoResult = new Page<>();

        private boolean listUsersInvoked;
        private boolean getUserDetailInvoked;
        private boolean getUserWalletInvoked;
        private boolean getUserWalletFlowInvoked;
        private boolean getConsumeInfoInvoked;

        private UserListQueryRequest capturedListRequest;
        private Long capturedUserId;
        private Long capturedWalletUserId;
        private Long capturedFlowUserId;
        private Long capturedConsumeUserId;
        private PageRequest capturedFlowPageRequest;
        private PageRequest capturedConsumePageRequest;

        @Override
        public Page<UserListDto> listUsers(UserListQueryRequest request) {
            this.listUsersInvoked = true;
            this.capturedListRequest = request;
            return userPage;
        }

        @Override
        public UserDetailDto getUserDetailById(Long userId) {
            this.getUserDetailInvoked = true;
            this.capturedUserId = userId;
            return userDetail;
        }

        @Override
        public UserWalletDto getUserWalletByUserId(Long userId) {
            this.getUserWalletInvoked = true;
            this.capturedWalletUserId = userId;
            return userWallet;
        }

        @Override
        public Page<UserWalletFlowDto> getUserWalletFlow(Long userId, PageRequest request) {
            this.getUserWalletFlowInvoked = true;
            this.capturedFlowUserId = userId;
            this.capturedFlowPageRequest = request;
            return walletFlowResult;
        }

        @Override
        public Page<UserConsumeInfoDto> getConsumeInfo(Long userId, PageRequest request) {
            this.getConsumeInfoInvoked = true;
            this.capturedConsumeUserId = userId;
            this.capturedConsumePageRequest = request;
            return consumeInfoResult;
        }
    }
}
