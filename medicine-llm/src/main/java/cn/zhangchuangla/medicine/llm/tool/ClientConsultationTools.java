package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import cn.zhangchuangla.medicine.llm.model.tool.ClientMallProductOut;
import cn.zhangchuangla.medicine.llm.model.tool.ClientSearchMallProductOut;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProvider;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProviderLoader;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 面向大模型的客户端咨询工具，暴露聊天支持的卡片/消息类型及示例，
 * 以及真实商品/订单数据获取能力，便于大模型按需拼装 SSE 返回数据。
 */
@Component
public class ClientConsultationTools {


    private final ClientDataProviderLoader providerLoader;
    private final SseMessageInjector messageInjector;

    public ClientConsultationTools(ClientDataProviderLoader providerLoader, SseMessageInjector messageInjector) {
        this.providerLoader = providerLoader;
        this.messageInjector = messageInjector;
    }

    /**
     * 搜索商城药品,用于当AI诊断完病情之后获取药品信息
     *
     * @param keyword 关键字
     * @param limit   最大返回数量
     * @return 商品列表
     */
    @Tool(description = "获取商城药品信息")
    public List<ClientSearchMallProductOut> searchMallProducts(@ToolParam(description = "搜索关键字,可以搜索商品(药品)可以搜索生病的症状") String keyword,
                                                               @ToolParam(description = "最大返回数量,默认为10") int limit) {
        if (keyword == null) {
            return List.of();
        }
        return requireProvider().searchMallProducts(keyword, limit);
    }

    /**
     * 根据药品ID获取药品详细信息
     *
     * @param id 商品ID
     * @return 商品详细信息
     */
    @Tool(description = "根据药品ID获取药品详细信息")
    public ClientMallProductOut getMallProductById(@ToolParam(description = "药品ID") Long id) {
        if (id == null) {
            return null;
        }
        return requireProvider().getMallProductById(id);
    }


    private ClientChatResponse buildNotice(String content) {
        ClientChatResponse resp = new ClientChatResponse();
        resp.setRole(MessageRole.ASSISTANT);
        resp.setContent(content);
        return resp;
    }

    private ClientDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 client SPI 提供者，请检查 client 模块是否已注册"));
    }
}
