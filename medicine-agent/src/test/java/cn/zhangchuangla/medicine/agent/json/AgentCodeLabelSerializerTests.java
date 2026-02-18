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

        assertEquals("上架", node.get("statusName").asText());
    }

    @Test
    void shouldUseDictMappingWhenPairsNotConfigured() {
        DictSample sample = new DictSample();
        sample.setDeliveryType(2);

        JsonNode node = serializeToNode(sample);

        assertEquals("快递配送", node.get("deliveryTypeName").asText());
    }

    @Test
    void shouldFallbackToSourceWhenMappingMissing() {
        UnknownCodeSample sample = new UnknownCodeSample();
        sample.setPayType("MIX_PAY");

        JsonNode node = serializeToNode(sample);

        assertEquals("MIX_PAY", node.get("payTypeName").asText());
    }

    @Test
    void shouldReturnNullWhenSourceFieldIsNull() {
        UnknownCodeSample sample = new UnknownCodeSample();
        sample.setPayType(null);

        JsonNode node = serializeToNode(sample);

        assertTrue(node.get("payTypeName").isNull());
    }

    @Test
    void shouldSupportIntegerCodeInPairs() {
        PairSample sample = new PairSample();
        sample.setStatus(0);

        JsonNode node = serializeToNode(sample);

        assertEquals("下架", node.get("statusName").asText());
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

        private Integer status;

        @AgentCodeLabel(
                source = "status",
                pairs = {
                        @AgentCodePair(code = "1", label = "上架"),
                        @AgentCodePair(code = "0", label = "下架")
                }
        )
        private String statusName = "";
    }

    @Data
    private static class DictSample {

        private Integer deliveryType;

        @AgentCodeLabel(source = "deliveryType", dictKey = AgentCodeLabelRegistry.AGENT_PRODUCT_DELIVERY_TYPE)
        private String deliveryTypeName = "";
    }

    @Data
    private static class UnknownCodeSample {

        private String payType;

        @AgentCodeLabel(source = "payType", dictKey = AgentCodeLabelRegistry.AGENT_ORDER_PAY_TYPE)
        private String payTypeName = "";
    }
}
