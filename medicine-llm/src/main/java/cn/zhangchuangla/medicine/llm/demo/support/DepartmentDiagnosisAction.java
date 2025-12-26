package cn.zhangchuangla.medicine.llm.demo.support;

/**
 * 科室诊断能力抽象
 * <p>
 * 负责：为不同科室提供统一的“基于摘要生成诊断建议”的接口，供 {@code ExpertDiagnosisNodeAction} 按路由选择具体实现。
 */
public interface DepartmentDiagnosisAction {

    String diagnose(String summary);
}
