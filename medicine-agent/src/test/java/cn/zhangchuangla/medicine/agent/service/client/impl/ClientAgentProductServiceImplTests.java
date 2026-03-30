package cn.zhangchuangla.medicine.agent.service.client.impl;

import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import cn.zhangchuangla.medicine.rpc.client.ClientAgentProductRpcService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAgentProductServiceImplTests {

    @Mock
    private ClientAgentProductRpcService clientAgentProductRpcService;

    @InjectMocks
    private ClientAgentProductServiceImpl service;

    @Test
    void searchProducts_WhenRpcReturnsNull_ShouldReturnEmptyPage() {
        when(clientAgentProductRpcService.searchProducts(new ClientAgentProductSearchRequest())).thenReturn(null);

        var page = service.searchProducts(null);

        assertEquals(0L, page.getTotal());
        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void searchProducts_ShouldConvertPageResult() {
        ClientAgentProductSearchRequest request = new ClientAgentProductSearchRequest();
        request.setKeyword("感冒灵");
        PageResult<ClientAgentProductSearchDto> rpcResult = new PageResult<>(1L, 10L, 1L, List.of(
                ClientAgentProductSearchDto.builder().productId(1L).productName("999感冒灵颗粒").build()
        ));
        when(clientAgentProductRpcService.searchProducts(request)).thenReturn(rpcResult);

        var page = service.searchProducts(request);

        assertEquals(1L, page.getTotal());
        assertEquals(1, page.getRecords().size());
    }

    @Test
    void getProductSpec_ShouldDelegateToRpc() {
        ClientAgentProductSpecDto specDto = ClientAgentProductSpecDto.builder().productId(1L).build();
        when(clientAgentProductRpcService.getProductSpec(1L)).thenReturn(specDto);

        assertSame(specDto, service.getProductSpec(1L));
    }

    @Test
    void getProductCards_ShouldDelegateToRpc() {
        ClientAgentProductCardsDto rpcResult = ClientAgentProductCardsDto.builder()
                .totalPrice("36.70")
                .items(List.of(
                        ClientAgentProductCardsDto.ClientAgentProductItemDto.builder()
                                .id("102")
                                .name("维生素C咀嚼片")
                                .build()
                ))
                .build();
        when(clientAgentProductRpcService.getProductCards(List.of(102L))).thenReturn(rpcResult);

        var result = service.getProductCards(List.of(102L));

        assertEquals("36.70", result.getTotalPrice());
        assertEquals(1, result.getItems().size());
        assertEquals("102", result.getItems().getFirst().getId());
    }

    @Test
    void getProductCards_WhenRpcReturnsNull_ShouldReturnEmptyCards() {
        when(clientAgentProductRpcService.getProductCards(List.of(999L))).thenReturn(null);

        var result = service.getProductCards(List.of(999L));

        assertEquals("0.00", result.getTotalPrice());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getProductPurchaseCards_ShouldDelegateToRpc() {
        List<ClientAgentProductPurchaseQueryDto> items = List.of(
                ClientAgentProductPurchaseQueryDto.builder().productId(102L).quantity(2).build()
        );
        ClientAgentProductPurchaseCardsDto rpcResult = ClientAgentProductPurchaseCardsDto.builder()
                .totalPrice(new BigDecimal("39.80"))
                .items(List.of(
                        ClientAgentProductPurchaseCardsDto.ClientAgentProductPurchaseItemDto.builder()
                                .id("102")
                                .name("维生素C咀嚼片")
                                .price(new BigDecimal("19.90"))
                                .quantity(2)
                                .build()
                ))
                .build();
        when(clientAgentProductRpcService.getProductPurchaseCards(items)).thenReturn(rpcResult);

        var result = service.getProductPurchaseCards(items);

        assertEquals(new BigDecimal("39.80"), result.getTotalPrice());
        assertEquals(1, result.getItems().size());
        assertEquals("102", result.getItems().getFirst().getId());
        assertEquals(2, result.getItems().getFirst().getQuantity());
    }

    @Test
    void getProductPurchaseCards_WhenRpcReturnsNull_ShouldReturnEmptyCards() {
        List<ClientAgentProductPurchaseQueryDto> items = List.of(
                ClientAgentProductPurchaseQueryDto.builder().productId(999L).quantity(1).build()
        );
        when(clientAgentProductRpcService.getProductPurchaseCards(items)).thenReturn(null);

        var result = service.getProductPurchaseCards(items);

        assertEquals(new BigDecimal("0.00"), result.getTotalPrice());
        assertTrue(result.getItems().isEmpty());
    }
}
