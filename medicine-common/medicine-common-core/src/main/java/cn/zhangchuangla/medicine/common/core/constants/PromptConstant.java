package cn.zhangchuangla.medicine.common.core.constants;

/**
 * 医疗工作流提示词常量类
 *
 * @author Chuang
 * @since 2025/9/10
 */
public class PromptConstant {

    /**
     * 意图识别提示词
     */
    public static final String INTENT_PROMPT = """
            请分析以下用户输入，严格输出下列三种英文小写之一（仅输出该词，不要输出其他任何内容）：
            - medicine：用户想购买药品或询问药品信息
            - consult：用户咨询健康问题或寻求医疗建议
            - other：其他或无法识别
            用户输入：%s
            """;

    /**
     * 药品咨询提示词
     */
    public static final String MEDICINE_PROMPT = """
            用户希望购买药品或咨询药品信息。请给出专业、清晰且可执行的回复（包含成分/适应症/禁忌/用法用量/就诊建议等必要要点）。
            注意：请提示用户遵医嘱，必要时线下就医。
            用户输入：%s
            """;

    /**
     * 健康咨询提示词
     */
    public static final String CONSULT_PROMPT = """
            用户咨询健康问题。请提供循证友好的建议，结构化分点输出（可能原因、初步建议、何时就医、紧急警示）。
            声明：此为一般性健康建议，不能替代医生面诊。
            用户输入：%s
            """;

    /**
     * 其他问题提示词
     */
    public static final String OTHER_PROMPT = """
            用户的问题目前用于测试,你需要回答任何问题,方便开发人员测试功能,并且根据用户的消息正常回复
            用户输入：%s
            """;

    /**
     * 默认错误回复
     */
    public static final String DEFAULT_ERROR_REPLY = "抱歉，暂时无法处理您的问题，请联系人工客服获取帮助。";

    /**
     * 药品咨询错误回复
     */
    public static final String MEDICINE_ERROR_REPLY = "抱歉，暂时无法处理您的药品咨询问题，请稍后再试或联系人工客服。";

    /**
     * 健康咨询错误回复
     */
    public static final String CONSULT_ERROR_REPLY = "抱歉，暂时无法处理您的健康咨询问题，请稍后再试或联系人工客服。";
}
