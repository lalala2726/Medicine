package cn.zhangchuangla.medicine.agent.json;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentCodePair;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentCodeLabelSerializerTests {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void shouldUsePairsMappingWhenMatched() {
        PairSample sample = new PairSample();
        sample.setStatus(1);

        JsonNode node = serializeToNode(sample);

        assertEquals("上架", node.get("status").get("description").asText());
        assertEquals(1, node.get("status").get("value").asInt());
    }

    @Test
    void shouldUseDictMappingWhenPairsNotConfigured() {
        DictSample sample = new DictSample();
        sample.setDeliveryType(2);

        JsonNode node = serializeToNode(sample);

        assertEquals("快递配送", node.get("deliveryType").get("description").asText());
        assertEquals(2, node.get("deliveryType").get("value").asInt());
    }

    @Test
    void shouldFallbackToSourceWhenMappingMissing() {
        UnknownCodeSample sample = new UnknownCodeSample();
        sample.setPayType("MIX_PAY");

        JsonNode node = serializeToNode(sample);

        assertEquals("MIX_PAY", node.get("payType").get("description").asText());
        assertEquals("MIX_PAY", node.get("payType").get("value").asText());
    }

    @Test
    void shouldReturnNullWhenSourceFieldIsNull() {
        UnknownCodeSample sample = new UnknownCodeSample();
        sample.setPayType(null);

        JsonNode node = serializeToNode(sample);

        assertTrue(node.get("payType").isNull());
    }

    @Test
    void shouldSupportIntegerCodeInPairs() {
        PairSample sample = new PairSample();
        sample.setStatus(0);

        JsonNode node = serializeToNode(sample);

        assertEquals("下架", node.get("status").get("description").asText());
        assertTrue(node.get("status").get("value").isInt());
        assertEquals(0, node.get("status").get("value").asInt());
    }

    @Test
    void shouldResolveSourceFieldWhenConfigured() {
        SourceFieldSample sample = new SourceFieldSample();
        sample.setChangeType("订单支付");
        sample.setAmountDirection(2);

        JsonNode node = serializeToNode(sample);

        assertEquals(2, node.get("changeType").get("value").asInt());
        assertEquals("支出", node.get("changeType").get("description").asText());
        assertEquals(2, node.get("amountDirection").get("value").asInt());
        assertEquals("支出", node.get("amountDirection").get("description").asText());
    }

    private JsonNode serializeToNode(Object value) {
        try {
            return objectMapper.readTree(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("serialize value failed", ex);
        }
    }

    @Data
    private static class PairSample {

        @AgentCodeLabel(
                pairs = {
                        @AgentCodePair(code = "1", label = "上架"),
                        @AgentCodePair(code = "0", label = "下架")
                }
        )
        private Integer status;
    }

    @Data
    private static class DictSample {

        @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_PRODUCT_DELIVERY_TYPE)
        private Integer deliveryType;
    }

    @Data
    private static class UnknownCodeSample {

        @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_ORDER_PAY_TYPE)
        private String payType;
    }

    @Data
    private static class SourceFieldSample {

        @AgentCodeLabel(source = "amountDirection", dictKey = AgentCodeLabelRegistry.AGENT_USER_WALLET_CHANGE_TYPE)
        private String changeType;

        @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_USER_WALLET_CHANGE_TYPE)
        private Integer amountDirection;
    }
}
