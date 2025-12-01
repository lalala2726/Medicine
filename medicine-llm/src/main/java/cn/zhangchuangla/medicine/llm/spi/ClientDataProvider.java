package cn.zhangchuangla.medicine.llm.spi;

import cn.zhangchuangla.medicine.llm.model.tool.client.ClientMallProductOut;
import cn.zhangchuangla.medicine.llm.model.tool.client.ClientSearchMallProductOut;

import java.util.List;

/**
 * 由 client 模块通过 SPI 提供的用户侧数据，供 LLM 生成卡片消息使用。
 */
public interface ClientDataProvider {

    List<ClientSearchMallProductOut> searchMallProducts(String keyword, int limit);

    ClientMallProductOut getMallProductById(Long id);

    List<ClientMallProductOut> getMallProductById(List<Long> ids);
}
