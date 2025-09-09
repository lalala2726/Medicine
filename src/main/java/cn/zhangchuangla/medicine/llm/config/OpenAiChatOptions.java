package cn.zhangchuangla.medicine.llm.config;

import lombok.Builder;
import lombok.Data;

/**
 * OpenAI聊天选项配置
 * 基于Spring AI官方配置
 *
 * @author Chuang
 * @since 2025/9/6
 */
@Data
@Builder
public class OpenAiChatOptions {
    
    /**
     * 模型名称
     */
    private String model;
    
    
    /**
     * 顶层P值
     */
    private Double topP;
    
    /**
     * 频率惩罚
     */
    private Double frequencyPenalty;
    
    /**
     * 存在惩罚
     */
    private Double presencePenalty;
}