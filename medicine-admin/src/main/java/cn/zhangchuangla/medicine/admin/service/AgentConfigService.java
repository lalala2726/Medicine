package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.*;

import java.util.List;

/**
 * Agent 配置服务。
 */
public interface AgentConfigService {

    /**
     * 查询知识库 Agent 配置详情。
     *
     * @return 知识库 Agent 配置
     */
    KnowledgeBaseAgentConfigVo getKnowledgeBaseConfig();

    /**
     * 保存知识库 Agent 配置。
     *
     * @param request 知识库 Agent 配置请求
     * @return 是否保存成功
     */
    boolean saveKnowledgeBaseConfig(KnowledgeBaseAgentConfigRequest request);

    /**
     * 查询知识库下拉选项列表。
     *
     * @return 知识库下拉选项
     */
    List<KnowledgeBaseOptionVo> listKnowledgeBaseOptions();

    /**
     * 查询管理端助手 Agent 配置详情。
     *
     * @return 管理端助手 Agent 配置
     */
    AdminAssistantAgentConfigVo getAdminAssistantConfig();

    /**
     * 查询客户端助手 Agent 配置详情。
     *
     * @return 客户端助手 Agent 配置
     */
    ClientAssistantAgentConfigVo getClientAssistantConfig();

    /**
     * 保存管理端助手 Agent 配置。
     *
     * @param request 管理端助手 Agent 配置请求
     * @return 是否保存成功
     */
    boolean saveAdminAssistantConfig(AdminAssistantAgentConfigRequest request);

    /**
     * 保存客户端助手 Agent 配置。
     *
     * @param request 客户端助手 Agent 配置请求
     * @return 是否保存成功
     */
    boolean saveClientAssistantConfig(ClientAssistantAgentConfigRequest request);

    /**
     * 查询图片识别 Agent 配置详情。
     *
     * @return 图片识别 Agent 配置
     */
    ImageRecognitionAgentConfigVo getImageRecognitionConfig();

    /**
     * 查询豆包语音 Agent 配置详情。
     *
     * @return 豆包语音 Agent 配置
     */
    SpeechAgentConfigVo getSpeechConfig();

    /**
     * 保存图片识别 Agent 配置。
     *
     * @param request 图片识别 Agent 配置请求
     * @return 是否保存成功
     */
    boolean saveImageRecognitionConfig(ImageRecognitionAgentConfigRequest request);

    /**
     * 保存豆包语音 Agent 配置。
     *
     * @param request 豆包语音 Agent 配置请求
     * @return 是否保存成功
     */
    boolean saveSpeechConfig(SpeechAgentConfigRequest request);

    /**
     * 查询聊天历史总结 Agent 配置详情。
     *
     * @return 聊天历史总结 Agent 配置
     */
    ChatHistorySummaryAgentConfigVo getChatHistorySummaryConfig();

    /**
     * 保存聊天历史总结 Agent 配置。
     *
     * @param request 聊天历史总结 Agent 配置请求
     * @return 是否保存成功
     */
    boolean saveChatHistorySummaryConfig(ChatHistorySummaryAgentConfigRequest request);

    /**
     * 查询聊天标题生成 Agent 配置详情。
     *
     * @return 聊天标题生成 Agent 配置
     */
    ChatTitleAgentConfigVo getChatTitleConfig();

    /**
     * 保存聊天标题生成 Agent 配置。
     *
     * @param request 聊天标题生成 Agent 配置请求
     * @return 是否保存成功
     */
    boolean saveChatTitleConfig(ChatTitleAgentConfigRequest request);

    /**
     * 查询向量模型选项列表。
     *
     * @return 向量模型选项
     */
    List<AgentModelOptionVo> listEmbeddingModelOptions();

    /**
     * 查询聊天模型选项列表。
     *
     * @return 聊天模型选项
     */
    List<AgentModelOptionVo> listChatModelOptions();

    /**
     * 查询图片理解模型选项列表。
     *
     * @return 图片理解模型选项
     */
    List<AgentModelOptionVo> listVisionModelOptions();
}
