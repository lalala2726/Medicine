package cn.zhangchuangla.medicine.llm.workflow.node;

import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 摘要整理节点
 *
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Component
@RequiredArgsConstructor
public class SummaryCollationNode implements NodeAction {

    private static final String PROMPT = """
            # Role
            你是一名专业的**医疗临床信息结构化专家**，运行在医疗诊断工作流的数据预处理节点。
            
            # Goal
            接收上一级传输的医患对话记录（Chat Log），提取所有对临床诊断有价值的医学特征，剔除所有无关噪音，输出标准化的结构化数据。
            
            # Input
            - 输入内容：医患双方的原始聊天记录文本。
            
            # Workflow
            1. **清洗**：过滤掉所有的寒暄（“你好”、“谢谢”）、情绪发泄（除非涉及精神类症状）、无意义的语气词及与病情无关的闲聊。
            2. **提取**：从对话中分析并提取以下关键维度的信息。如果某项信息未提及，请标记为 null。
            3. **输出**：生成符合要求的 JSON 数据。
            
            # Extraction Rules (提取规则)
            你需要关注以下维度的细节：
            - **基本信息 (Demographics)**：年龄、性别、职业（若有）。
            - **主诉 (Chief Complaint)**：患者最主要的不适症状。
            - **现病史 (HPI)**：
              - 症状表现：具体的疼痛性质、部位、程度。
              - 时间线：症状持续了多久，起病急缓。
              - 诱因/加重/缓解因素：什么情况下会痛，吃什么药会好转等。
            - **既往史 (Medical History)**：是否有慢性病、手术史、过敏史。
            - **用药情况 (Medication)**：当前正在使用的药物。
            - **其他细节**：睡眠、饮食、大小便情况等一般状况。
            
            # Constraints
            - **严禁臆测**：只能提取对话中明确提到的信息，不可通过推断补全缺失信息。
            - **保持原意**：对于描述症状的专业术语或患者的特殊描述，请尽量保持原意，不要过度概括导致细节丢失。
            - **客观中立**：去除带有主观色彩的非医疗描述。
            """;
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String originalText = String.valueOf(state.value("originalText"));
        String result = chatClient.prompt(PROMPT)
                .user(originalText)
                .call()
                .content();
        if (result == null) {
            throw new LLMParamException("摘要整理结果为空");
        }
        return Map.of("summary", result);
    }
}
