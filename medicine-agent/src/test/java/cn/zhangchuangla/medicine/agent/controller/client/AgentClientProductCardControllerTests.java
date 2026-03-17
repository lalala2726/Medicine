package cn.zhangchuangla.medicine.agent.controller.client;

import cn.zhangchuangla.medicine.agent.advice.AgentResponseDescriptionAdvice;
import cn.zhangchuangla.medicine.agent.service.client.ClientAgentProductService;
import cn.zhangchuangla.medicine.agent.support.AgentVoDescriptionResolver;
import cn.zhangchuangla.medicine.common.core.exception.GlobalExceptionHandel;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductPurchaseCardsDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSearchDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgentClientProductCardControllerTests {

    private final StubClientAgentProductService productService = new StubClientAgentProductService();
    private final AgentClientProductCardController controller = new AgentClientProductCardController(productService);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        AgentResponseDescriptionAdvice descriptionAdvice =
                new AgentResponseDescriptionAdvice(new AgentVoDescriptionResolver(), objectMapper);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandel(), descriptionAdvice)
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProductPurchaseCards_ShouldMapItemsAndPreserveOrder() {
        setupAuthentication(99L);
        productService.productPurchaseCards = createProductPurchaseCards();

        var result = controller.getProductPurchaseCards(List.of(102L, 101L));

        assertEquals(200, result.getCode());
        assertTrue(productService.purchaseCardsInvoked);
        assertEquals(List.of(102L, 101L), productService.capturedPurchaseCardProductIds);
        assertNotNull(result.getData());
        assertEquals("36.70", result.getData().getTotalPrice());
        assertEquals(2, result.getData().getItems().size());
        assertEquals("102", result.getData().getItems().get(0).getId());
        assertEquals("101", result.getData().getItems().get(1).getId());
    }

    @Test
    void getProductPurchaseCards_ShouldAppendMeta() throws Exception {
        setupAuthentication(99L);
        productService.productPurchaseCards = createProductPurchaseCards();

        mockMvc.perform(get("/agent/client/product/purchase-cards/102,101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPrice").value("36.70"))
                .andExpect(jsonPath("$.data.items[0].id").value("102"))
                .andExpect(jsonPath("$.data.meta.entityDescription").value("客户端智能体商品购买卡片"))
                .andExpect(jsonPath("$.data.meta.fieldDescriptions.totalPrice").value("整体价格"));
    }

    private void setupAuthentication(Long userId) {
        AuthUser authUser = AuthUser.builder().id(userId).username("client_user").build();
        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_user")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    private ClientAgentProductPurchaseCardsDto createProductPurchaseCards() {
        return ClientAgentProductPurchaseCardsDto.builder()
                .totalPrice("36.70")
                .items(List.of(
                        ClientAgentProductPurchaseCardsDto.ClientAgentProductPurchaseItemDto.builder()
                                .id("102")
                                .name("维生素C咀嚼片")
                                .image("https://example.com/102.png")
                                .price("19.90")
                                .spec("60片/瓶")
                                .efficacy("补充维生素C")
                                .prescription(false)
                                .stock(98)
                                .build(),
                        ClientAgentProductPurchaseCardsDto.ClientAgentProductPurchaseItemDto.builder()
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

    private static class StubClientAgentProductService implements ClientAgentProductService {

        private ClientAgentProductPurchaseCardsDto productPurchaseCards;
        private boolean purchaseCardsInvoked;
        private List<Long> capturedPurchaseCardProductIds;

        @Override
        public Page<ClientAgentProductSearchDto> searchProducts(ClientAgentProductSearchRequest request) {
            throw new UnsupportedOperationException("not needed in this test");
        }

        @Override
        public MallProductDetailDto getProductDetail(Long productId) {
            throw new UnsupportedOperationException("not needed in this test");
        }

        @Override
        public ClientAgentProductPurchaseCardsDto getProductPurchaseCards(List<Long> productIds) {
            this.purchaseCardsInvoked = true;
            this.capturedPurchaseCardProductIds = productIds;
            return productPurchaseCards;
        }

        @Override
        public ClientAgentProductSpecDto getProductSpec(Long productId) {
            throw new UnsupportedOperationException("not needed in this test");
        }
    }
}
