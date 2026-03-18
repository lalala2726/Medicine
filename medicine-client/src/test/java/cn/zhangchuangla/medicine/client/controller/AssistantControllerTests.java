package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.service.MallOrderService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssistantControllerTests {

    @Mock
    private MallOrderService mallOrderService;

    @Mock
    private MallProductService mallProductService;

    @InjectMocks
    private AssistantController controller;

}
