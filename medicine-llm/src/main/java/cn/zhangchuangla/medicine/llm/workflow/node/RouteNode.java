package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Component
@RequiredArgsConstructor
public class RouteNode implements NodeAction {

    private final static String PROMPT = """
            # Role
            你是一个**智能医疗分诊路由系统 (Intelligent Medical Triage Router)**。你的唯一任务是根据输入的病情摘要，将患者精准路由到下游对应的专家处理节点。
            
            # Input Data
            你将接收来自上一节点的“病情摘要”信息，通常包含：主诉、现病史、症状、基本信息（年龄/性别）等。
            
            # Routing Taxonomy (允许的路由节点)
            你必须且只能从以下 4 个节点中选择最匹配的一个：
            
            1. **InternalMedicineExpertNode**
               - 适用范围：发热、感冒、头痛、腹痛、高血压、糖尿病、消化系统问题、呼吸系统问题、以及无法归类到其他科室的一般性身体不适。
               - 注意：如果患者是儿童且无外科/皮肤/心理特征，请归类到此节点。
            
            2. **SurgeryExpertNode**
               - 适用范围：外伤、骨折、开放性伤口、动物咬伤、体表肿物、剧烈且需要外科干预的疼痛（如阑尾炎特征）、烧烫伤。
            
            3. **DermatologyExpertNode**
               - 适用范围：皮疹、瘙痒、痘痘、脱发、皮肤过敏、体表斑块、蚊虫叮咬引起的皮肤反应。
            
            4. **PsychologyExpertNode**
               - 适用范围：失眠、焦虑、抑郁、情绪低落、幻觉、胡言乱语、精神压力大、想轻生。
            
            # Critical Rules (核心铁律)
            1. **严格输出格式**：输出必须严格是上述 4 个节点名称之一（例如 `SurgeryExpertNode`）。
            2. **禁止废话**：**严禁**输出任何解释、分析过程、Markdown 标记、JSON 括号或标点符号。只输出节点名字符串。
            3. **唯一性**：只输出**一个**最匹配的结果。
            4. **兜底策略**：如果症状模糊或无法明确归类（且不涉及外伤、皮肤或心理），请默认路由至 **InternalMedicineExpertNode**。
            
            # Few-Shot Examples (示例)
            
            **Input:**
            {"summary": "患者主诉右小腿被流浪狗咬伤，有出血，疼痛明显。"}
            **Output:**
            SurgeryExpertNode
            
            **Input:**
            {"summary": "主诉发热3天，体温39度，伴有咽喉肿痛，咳嗽咳痰。"}
            **Output:**
            InternalMedicineExpertNode
            
            **Input:**
            {"patient_profile": {"age": "6岁"}, "summary": "孩子全身起了很多红疙瘩，很痒，一直抓。"}
            **Output:**
            DermatologyExpertNode
            
            **Input:**
            {"summary": "最近总是睡不着觉，心情很低落，觉得活着没意思，工作压力大。"}
            **Output:**
            PsychologyExpertNode
            
            **Input:**
            {"summary": "肚子有点不舒服，隐隐作痛，不知道吃了什么坏东西。"}
            **Output:**
            InternalMedicineExpertNode
            
            # Execution
            现在，请根据输入的病情摘要，输出唯一的路由节点名称：
            """;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String summary = String.valueOf(state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey()));
        String content = chatClient.prompt(PROMPT)
                .user(summary)
                .call()
                .content();
        if (content == null){
            throw new LLMParamException("路由结果为空");
        }
        return Map.of("route", content);
    }
}
