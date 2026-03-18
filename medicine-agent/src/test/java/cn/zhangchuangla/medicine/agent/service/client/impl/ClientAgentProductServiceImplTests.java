package cn.zhangchuangla.medicine.agent.service.client.impl;

import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSearchDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import cn.zhangchuangla.medicine.rpc.client.ClientAgentProductRpcService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
