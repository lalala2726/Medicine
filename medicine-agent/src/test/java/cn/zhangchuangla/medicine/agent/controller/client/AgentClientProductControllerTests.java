package cn.zhangchuangla.medicine.agent.controller.client;

import cn.zhangchuangla.medicine.agent.advice.AgentResponseDescriptionAdvice;
import cn.zhangchuangla.medicine.agent.model.vo.client.ClientAgentProductSearchVo;
import cn.zhangchuangla.medicine.agent.service.client.ClientAgentProductService;
import cn.zhangchuangla.medicine.agent.support.AgentVoDescriptionResolver;
import cn.zhangchuangla.medicine.common.core.exception.GlobalExceptionHandel;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSearchDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgentClientProductControllerTests {

    private final StubClientAgentProductService productService = new StubClientAgentProductService();
    private final AgentClientProductController controller = new AgentClientProductController(productService);
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
    void searchProducts_ShouldDelegateToService() {
        setupAuthentication(99L);
        ClientAgentProductSearchRequest request = new ClientAgentProductSearchRequest();
        request.setKeyword("  感冒灵  ");
        request.setPageNum(1);
        request.setPageSize(10);
        productService.searchPage = createSearchPage();

        var result = controller.searchProducts(request);

        assertEquals(200, result.getCode());
        assertTrue(productService.searchInvoked);
        assertEquals("感冒灵", productService.capturedRequest.getKeyword());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRows().size());
        assertInstanceOf(ClientAgentProductSearchVo.class, result.getData().getRows().getFirst());
    }

    @Test
    void getProductDetail_ShouldMapDrugDetail() {
        setupAuthentication(99L);
        productService.productDetail = createProductDetail();

        var result = controller.getProductDetail(1L);

        assertEquals(200, result.getCode());
        assertTrue(productService.detailInvoked);
        assertEquals(1L, productService.capturedProductId);
        assertNotNull(result.getData());
        assertEquals("999感冒灵颗粒", result.getData().getName());
        assertNotNull(result.getData().getDrugDetail());
        assertEquals("缓解普通感冒症状", result.getData().getDrugDetail().getEfficacy());
        assertEquals(List.of("https://example.com/p1.jpg"), result.getData().getImages());
    }

    @Test
    void getProductSpec_ShouldDelegateToService() {
        setupAuthentication(99L);
        productService.productSpec = ClientAgentProductSpecDto.builder()
                .productId(1L)
                .productName("999感冒灵颗粒")
                .packaging("10g*9袋")
                .composition("三叉苦")
                .build();

        var result = controller.getProductSpec(1L);

        assertEquals(200, result.getCode());
        assertTrue(productService.specInvoked);
        assertEquals(1L, productService.capturedSpecProductId);
        assertNotNull(result.getData());
        assertEquals("10g*9袋", result.getData().getPackaging());
    }

    @Test
    void searchProducts_ShouldAppendMeta() throws Exception {
        setupAuthentication(99L);
        productService.searchPage = createSearchPage();

        mockMvc.perform(get("/agent/client/product/search").param("keyword", "感冒灵"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rows[0].productName").value("999感冒灵颗粒"))
                .andExpect(jsonPath("$.data.meta.entityDescription").value("客户端智能体商品搜索结果"))
                .andExpect(jsonPath("$.data.meta.fieldDescriptions.productName").value("商品名称"));
    }

    @Test
    void searchProducts_ShouldRejectBlankKeyword() throws Exception {
        setupAuthentication(99L);

        mockMvc.perform(get("/agent/client/product/search").param("keyword", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("搜索关键词、分类名称、用途不能同时为空"));
    }

    @Test
    void searchProducts_ShouldAllowCategoryNameSearch() {
        setupAuthentication(99L);
        ClientAgentProductSearchRequest request = new ClientAgentProductSearchRequest();
        request.setCategoryName(" 感冒药 ");
        request.setPageNum(1);
        request.setPageSize(10);
        productService.searchPage = createSearchPage();

        var result = controller.searchProducts(request);

        assertEquals(200, result.getCode());
        assertEquals("感冒药", productService.capturedRequest.getCategoryName());
    }

    @Test
    void searchProducts_ShouldRejectPageSizeAboveLimit() throws Exception {
        setupAuthentication(99L);

        mockMvc.perform(get("/agent/client/product/search")
                        .param("keyword", "感冒灵")
                        .param("pageSize", "21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("每页数量不能超过20"));
    }

    @Test
    void searchProducts_ShouldRejectWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/agent/client/product/search").param("keyword", "感冒灵"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户未登录"));
    }

    private void setupAuthentication(Long userId) {
        AuthUser authUser = AuthUser.builder().id(userId).username("client_user").build();
        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_user")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    private Page<ClientAgentProductSearchDto> createSearchPage() {
        Page<ClientAgentProductSearchDto> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(ClientAgentProductSearchDto.builder()
                .productId(1L)
                .productName("999感冒灵颗粒")
                .cover("https://example.com/product.jpg")
                .price(new BigDecimal("29.90"))
                .build()));
        return page;
    }

    private MallProductDetailDto createProductDetail() {
        DrugDetailDto drugDetail = DrugDetailDto.builder()
                .commonName("复方感冒灵颗粒")
                .efficacy("缓解普通感冒症状")
                .usageMethod("开水冲服")
                .precautions("详见说明书")
                .instruction("完整说明书")
                .build();

        MallProductDetailDto detail = new MallProductDetailDto();
        detail.setId(1L);
        detail.setName("999感冒灵颗粒");
        detail.setCategoryId(2L);
        detail.setCategoryName("感冒药");
        detail.setPrice(new BigDecimal("29.90"));
        detail.setStock(100);
        detail.setStatus(1);
        detail.setDeliveryType(2);
        detail.setSales(99);
        detail.setImages(List.of("https://example.com/p1.jpg"));
        detail.setDrugDetail(drugDetail);
        return detail;
    }

    private static class StubClientAgentProductService implements ClientAgentProductService {

        private Page<ClientAgentProductSearchDto> searchPage = new Page<>();
        private MallProductDetailDto productDetail;
        private ClientAgentProductSpecDto productSpec;
        private boolean searchInvoked;
        private boolean detailInvoked;
        private boolean specInvoked;
        private ClientAgentProductSearchRequest capturedRequest;
        private Long capturedProductId;
        private Long capturedSpecProductId;

        @Override
        public Page<ClientAgentProductSearchDto> searchProducts(ClientAgentProductSearchRequest request) {
            this.searchInvoked = true;
            this.capturedRequest = request;
            return searchPage;
        }

        @Override
        public MallProductDetailDto getProductDetail(Long productId) {
            this.detailInvoked = true;
            this.capturedProductId = productId;
            return productDetail;
        }

        @Override
        public ClientAgentProductSpecDto getProductSpec(Long productId) {
            this.specInvoked = true;
            this.capturedSpecProductId = productId;
            return productSpec;
        }
    }
}
