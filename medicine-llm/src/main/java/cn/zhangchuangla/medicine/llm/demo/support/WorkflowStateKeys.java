package cn.zhangchuangla.medicine.llm.demo.support;

/**
 * 工作流状态键与节点名称常量
 * <p>
 * 负责：集中维护 StateGraph 中使用的 state key、路由标签、条件边分支标签、节点名称等，避免散落硬编码。
 */
public final class WorkflowStateKeys {

    // =========================
    // State Key（状态字段）
    // =========================

    // 病情摘要：基础问诊（摘要整理）后生成，后续追问/诊断均基于该摘要
    public static final String SUMMARY = "summary";
    // 科室路由结果：基础问诊后生成（INTERNAL_MEDICINE/SURGERY/DERMATOLOGY/GENERAL）
    public static final String ROUTE = "route";
    // 专家诊断结果：专家诊断节点输出，进入审核节点前的原始诊断建议
    public static final String DIAGNOSIS_RESULT = "diagnosisResult";
    // 最终输出结果：审核节点输出（通常为流式 Flux），对外返回给前端
    public static final String FINAL_RESULT = "finalResult";
    // 追问轮次：追问节点执行次数计数（可用于信息充分性判断/兜底策略）
    public static final String INQUIRY_ROUND = "inquiryRound";
    // 追问问题：追问节点生成并输出给用户的问题清单
    public static final String INQUIRY_QUESTIONS = "inquiryQuestions";
    // 追问回答：用户针对追问问题补充的回答内容（用于拼接到摘要参与诊断）
    public static final String INQUIRY_ANSWER = "inquiryAnswer";

    // =========================
    // 用户意图（初始分流）
    // =========================

    // 诊断意图：健康咨询/症状描述等，进入问诊-路由-追问-诊断流程
    public static final String USER_INTENT_DIAGNOSIS = "DIAGNOSIS";
    // 业务咨询意图：退货/订单/物流/支付/账号/用药说明等非诊断问题
    public static final String USER_INTENT_GENERAL_SERVICE = "GENERAL_SERVICE";

    // =========================
    // 科室路由标签（路由判断）
    // =========================

    // 内科方向：呼吸/消化/循环/慢病等常见内科问题
    public static final String ROUTE_INTERNAL_MEDICINE = "INTERNAL_MEDICINE";
    // 外科方向：外伤/伤口/烧烫伤/动物咬伤/急腹症等需要外科处置
    public static final String ROUTE_SURGERY = "SURGERY";
    // 皮肤科方向：皮疹/瘙痒/过敏/脱发/斑块等皮肤问题
    public static final String ROUTE_DERMATOLOGY = "DERMATOLOGY";
    // 综合方向：科室不明确/混合症状/其他，走综合追问兜底
    public static final String ROUTE_GENERAL = "GENERAL";

    // =========================
    // 信息检查分支（追问循环）
    // =========================

    // 信息充分：进入专家诊断节点
    public static final String INFO_CHECK_SUFFICIENT = "SUFFICIENT";
    // 信息不足：继续回到科室追问节点（当前实现默认走 SUFFICIENT）
    public static final String INFO_CHECK_INSUFFICIENT = "INSUFFICIENT";

    // =========================
    // 节点名称（StateGraph NodeId）
    // =========================

    // 初始询问节点：识别用户意图（业务咨询 / 进入诊断）
    public static final String NODE_INITIAL_INQUIRY = "InitialInquiryNodeAction";
    // 业务咨询节点：处理退货/订单/用药说明等非诊断问题，直接结束
    public static final String NODE_GENERAL_SERVICE = "GeneralServiceNodeAction";
    // 基础问诊节点：摘要整理 + 科室方向路由（route）初始化
    public static final String NODE_PRE_DIAGNOSIS = "PreDiagnosisNodeAction";

    // 内科专家追问节点：生成内科追问问题
    public static final String NODE_INTERNAL_MEDICINE_INQUIRY = "InternalMedicineInquiryNodeAction";
    // 外科专家追问节点：生成外科追问问题
    public static final String NODE_SURGERY_INQUIRY = "SurgeryInquiryNodeAction";
    // 皮肤科专家追问节点：生成皮肤科追问问题
    public static final String NODE_DERMATOLOGY_INQUIRY = "DermatologyInquiryNodeAction";
    // 综合专家追问节点：科室不明确时的兜底追问问题
    public static final String NODE_GENERAL_EXPERT_INQUIRY = "GeneralExpertInquiryNodeAction";

    // 专家诊断节点：根据 route 选择科室诊断实现，输出 diagnosisResult
    public static final String NODE_EXPERT_DIAGNOSIS = "ExpertDiagnosisNodeAction";
    // 风险审核节点：对 diagnosisResult 做合规/风控审核，输出 finalResult（流式）
    public static final String NODE_DIAGNOSIS_REVIEW = "DiagnosisReviewNodeAction";

    private WorkflowStateKeys() {
    }
}
