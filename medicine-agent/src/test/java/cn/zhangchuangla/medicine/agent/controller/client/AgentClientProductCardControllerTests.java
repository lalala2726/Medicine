package cn.zhangchuangla.medicine.agent.controller.client;

import cn.zhangchuangla.medicine.agent.advice.AgentResponseDescriptionAdvice;
import cn.zhangchuangla.medicine.agent.model.request.ClientAgentProductPurchaseCardsRequest;
import cn.zhangchuangla.medicine.agent.service.client.ClientAgentProductService;
import cn.zhangchuangla.medicine.agent.support.AgentVoDescriptionResolver;
import cn.zhangchuangla.medicine.common.core.exception.GlobalExceptionHandel;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.systemauth.config.SystemAuthProperties;
import cn.zhangchuangla.medicine.common.systemauth.core.SystemAuthCanonicalBuilder;
import cn.zhangchuangla.medicine.common.systemauth.core.SystemAuthClientRegistry;
import cn.zhangchuangla.medicine.common.systemauth.core.SystemAuthHeaders;
import cn.zhangchuangla.medicine.common.systemauth.core.SystemAuthSigner;
import cn.zhangchuangla.medicine.common.systemauth.inbound.AllowSystemAuthenticationFilter;
import cn.zhangchuangla.medicine.common.systemauth.inbound.AllowSystemEndpointRegistry;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgentClientProductCardControllerTests {

    private static final String SYSTEM_APP_ID = "medicine-admin";
    private static final String SYSTEM_SECRET = "secret-123";

    private final StubClientAgentProductService productService = new StubClientAgentProductService();
    private final AgentClientProductCardController controller = new AgentClientProductCardController(productService);
    private final SystemAuthCanonicalBuilder systemAuthCanonicalBuilder = new SystemAuthCanonicalBuilder();
    private final SystemAuthSigner systemAuthSigner = new SystemAuthSigner();
    private MockMvc mockMvc;
    private MockMvc mockMvcWithSystemAuth;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        AgentResponseDescriptionAdvice descriptionAdvice =
                new AgentResponseDescriptionAdvice(new AgentVoDescriptionResolver(), objectMapper);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandel(), descriptionAdvice)
                .setValidator(validator)
                .build();

        mockMvcWithSystemAuth = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandel(), descriptionAdvice)
                .setValidator(validator)
                .addFilters(createAllowSystemAuthenticationFilter())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProductCards_ShouldMapItemsAndPreserveOrder() {
        setupAuthentication(99L);
        productService.productCards = createProductCards();

        var result = controller.getProductCards(List.of(102L, 101L));

        assertEquals(200, result.getCode());
        assertTrue(productService.productCardsInvoked);
        assertEquals(List.of(102L, 101L), productService.capturedProductCardProductIds);
        assertNotNull(result.getData());
        assertEquals("36.70", result.getData().getTotalPrice());
        assertEquals(2, result.getData().getItems().size());
        assertEquals("102", result.getData().getItems().get(0).getId());
        assertEquals("101", result.getData().getItems().get(1).getId());
    }

    @Test
    void getProductCards_ShouldAppendMeta() throws Exception {
        setupAuthentication(99L);
        productService.productCards = createProductCards();

        mockMvc.perform(get("/agent/client/product/cards/102,101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPrice").value("36.70"))
                .andExpect(jsonPath("$.data.items[0].id").value("102"))
                .andExpect(jsonPath("$.data.meta.entityDescription").value("客户端智能体商品卡片"))
                .andExpect(jsonPath("$.data.meta.fieldDescriptions.totalPrice").value("整体价格"));
    }

    @Test
    void getProductPurchaseCards_WhenAuthorizationAndSystemHeadersProvided_ShouldPassFilter() throws Exception {
        productService.productPurchaseCards = createProductPurchaseCards();
        byte[] requestBody = objectMapper.writeValueAsBytes(createPurchaseRequest());
        SignedHeaders signedHeaders = buildSignedHeaders("POST", "/agent/client/purchase_cards", requestBody);

        mockMvcWithSystemAuth.perform(post("/agent/client/purchase_cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(SystemAuthHeaders.AUTHORIZATION, "Bearer abc")
                        .header(SystemAuthHeaders.X_AGENT_KEY, signedHeaders.agentKey())
                        .header(SystemAuthHeaders.X_AGENT_TIMESTAMP, signedHeaders.timestamp())
                        .header(SystemAuthHeaders.X_AGENT_NONCE, signedHeaders.nonce())
                        .header(SystemAuthHeaders.X_AGENT_SIGN_VERSION, signedHeaders.signVersion())
                        .header(SystemAuthHeaders.X_AGENT_SIGNATURE, signedHeaders.signature()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPrice").value(53.5))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));

        assertTrue(productService.purchaseCardsInvoked);
    }

    @Test
    void getProductPurchaseCards_ShouldMapItemsAndCalculateTotalPrice() {
        setupAuthentication(99L);
        productService.productPurchaseCards = createProductPurchaseCards();

        var result = controller.getProductPurchaseCards(createPurchaseRequest());

        assertEquals(200, result.getCode());
        assertTrue(productService.purchaseCardsInvoked);
        assertEquals(new BigDecimal("53.50"), result.getData().getTotalPrice());
        assertEquals(2, result.getData().getItems().size());
        assertEquals(List.of(
                ClientAgentProductPurchaseQueryDto.builder().productId(101L).quantity(2).build(),
                ClientAgentProductPurchaseQueryDto.builder().productId(205L).quantity(1).build()
        ), productService.capturedPurchaseItems);
        assertEquals(2, result.getData().getItems().get(0).getQuantity());
        assertEquals(1, result.getData().getItems().get(1).getQuantity());
    }

    @Test
    void getProductPurchaseCards_ShouldAppendMeta() throws Exception {
        setupAuthentication(99L);
        productService.productPurchaseCards = createProductPurchaseCards();

        mockMvc.perform(post("/agent/client/purchase_cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPurchaseRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPrice").value(53.5))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.meta.entityDescription").value("客户端智能体商品购买卡片结果"))
                .andExpect(jsonPath("$.data.meta.fieldDescriptions.totalPrice").value("整体总价"));
    }

    private void setupAuthentication(Long userId) {
        AuthUser authUser = AuthUser.builder().id(userId).username("client_user").build();
        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_user")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    private ClientAgentProductCardsDto createProductCards() {
        return ClientAgentProductCardsDto.builder()
                .totalPrice("36.70")
                .items(List.of(
                        ClientAgentProductCardsDto.ClientAgentProductItemDto.builder()
                                .id("102")
                                .name("维生素C咀嚼片")
                                .image("https://example.com/102.png")
                                .price("19.90")
                                .spec("60片/瓶")
                                .efficacy("补充维生素C")
                                .prescription(false)
                                .stock(98)
                                .build(),
                        ClientAgentProductCardsDto.ClientAgentProductItemDto.builder()
                                .id("101")
                                .name("布洛芬缓释胶囊")
                                .image("https://example.com/101.png")
                                .price("16.80")
                                .spec("24粒/盒")
                                .efficacy("缓解发热、头痛")
                                .prescription(false)
                                .stock(56)
                                .build()
                ))
                .build();
    }

    private ClientAgentProductPurchaseCardsDto createProductPurchaseCards() {
        return ClientAgentProductPurchaseCardsDto.builder()
                .totalPrice(new BigDecimal("53.50"))
                .items(List.of(
                        ClientAgentProductPurchaseCardsDto.ClientAgentProductPurchaseItemDto.builder()
                                .id("101")
                                .name("布洛芬缓释胶囊")
                                .image("https://example.com/101.png")
                                .price(new BigDecimal("16.80"))
                                .quantity(2)
                                .spec("24粒/盒")
                                .efficacy("缓解发热、头痛")
                                .prescription(false)
                                .stock(56)
                                .build(),
                        ClientAgentProductPurchaseCardsDto.ClientAgentProductPurchaseItemDto.builder()
                                .id("205")
                                .name("维生素C咀嚼片")
                                .image("https://example.com/205.png")
                                .price(new BigDecimal("19.90"))
                                .quantity(1)
                                .spec("60片/瓶")
                                .efficacy("补充维生素C")
                                .prescription(false)
                                .stock(98)
                                .build()
                ))
                .build();
    }

    private ClientAgentProductPurchaseCardsRequest createPurchaseRequest() {
        ClientAgentProductPurchaseCardsRequest request = new ClientAgentProductPurchaseCardsRequest();

        ClientAgentProductPurchaseCardsRequest.PurchaseItem first =
                new ClientAgentProductPurchaseCardsRequest.PurchaseItem();
        first.setProductId(101L);
        first.setQuantity(2);

        ClientAgentProductPurchaseCardsRequest.PurchaseItem second =
                new ClientAgentProductPurchaseCardsRequest.PurchaseItem();
        second.setProductId(205L);
        second.setQuantity(1);

        request.setItems(List.of(first, second));
        return request;
    }

    private AllowSystemAuthenticationFilter createAllowSystemAuthenticationFilter() {
        SystemAuthProperties properties = new SystemAuthProperties();
        properties.setEnabled(true);
        properties.setDefaultSignVersion("v1");
        properties.setMaxSkewSeconds(300);
        properties.setNonceTtlSeconds(600);
        properties.setNonceKeyPrefix("system_auth:nonce");

        AllowSystemEndpointRegistry endpointRegistry = mock(AllowSystemEndpointRegistry.class);
        when(endpointRegistry.requiresSystemAuth(any())).thenReturn(true);

        SystemAuthClientRegistry clientRegistry = mock(SystemAuthClientRegistry.class);
        when(clientRegistry.findEnabledClient(SYSTEM_APP_ID)).thenReturn(Optional.of(enabledClient()));

        @SuppressWarnings("unchecked")
        RedisTemplate<Object, Object> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<Object, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);

        return new AllowSystemAuthenticationFilter(
                properties,
                endpointRegistry,
                clientRegistry,
                systemAuthCanonicalBuilder,
                systemAuthSigner,
                redisTemplate
        );
    }

    private SignedHeaders buildSignedHeaders(String method, String path, byte[] body) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = UUID.randomUUID().toString();
        String canonical = systemAuthCanonicalBuilder.buildCanonical(
                method,
                path,
                "",
                timestamp,
                nonce,
                systemAuthCanonicalBuilder.sha256Hex(body)
        );
        String signature = systemAuthSigner.sign(SYSTEM_SECRET, canonical);
        return new SignedHeaders(SYSTEM_APP_ID, timestamp, nonce, "v1", signature);
    }

    private SystemAuthClientRegistry.SystemAuthClient enabledClient() {
        SystemAuthClientRegistry.SystemAuthClient client = new SystemAuthClientRegistry.SystemAuthClient();
        client.setAppId(SYSTEM_APP_ID);
        client.setSecret(SYSTEM_SECRET);
        client.setEnabled(true);
        return client;
    }

    private record SignedHeaders(String agentKey, String timestamp, String nonce, String signVersion,
                                 String signature) {
    }

    private static class StubClientAgentProductService implements ClientAgentProductService {

        private ClientAgentProductCardsDto productCards;
        private ClientAgentProductPurchaseCardsDto productPurchaseCards;
        private boolean productCardsInvoked;
        private boolean purchaseCardsInvoked;
        private List<Long> capturedProductCardProductIds;
        private List<ClientAgentProductPurchaseQueryDto> capturedPurchaseItems;

        @Override
        public Page<ClientAgentProductSearchDto> searchProducts(ClientAgentProductSearchRequest request) {
            throw new UnsupportedOperationException("not needed in this test");
        }

        @Override
        public MallProductDetailDto getProductDetail(Long productId) {
            throw new UnsupportedOperationException("not needed in this test");
        }

        @Override
        public ClientAgentProductCardsDto getProductCards(List<Long> productIds) {
            this.productCardsInvoked = true;
            this.capturedProductCardProductIds = productIds;
            return productCards;
        }

        @Override
        public ClientAgentProductPurchaseCardsDto getProductPurchaseCards(List<ClientAgentProductPurchaseQueryDto> items) {
            this.purchaseCardsInvoked = true;
            this.capturedPurchaseItems = items;
            return productPurchaseCards;
        }

        @Override
        public ClientAgentProductSpecDto getProductSpec(Long productId) {
            throw new UnsupportedOperationException("not needed in this test");
        }
    }
}
