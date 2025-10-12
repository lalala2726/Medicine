package cn.zhangchuangla.medicine.admin.ai.enums;

/**
 * 医疗工作流节点枚举
 *
 * @author Chuang
 * @since 2025/9/10
 */
public enum MedicineNodeEnum {

    INTENT("intent", "意图识别节点"),
    MEDICINE("medicine", "药品咨询节点"),
    CONSULT("consult", "健康咨询节点"),
    OTHER("other", "其他问题节点");

    private final String nodeId;
    private final String description;

    MedicineNodeEnum(String nodeId, String description) {
        this.nodeId = nodeId;
        this.description = description;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return nodeId;
    }
}
