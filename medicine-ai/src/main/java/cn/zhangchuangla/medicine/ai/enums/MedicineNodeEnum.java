package cn.zhangchuangla.medicine.ai.enums;

import lombok.Getter;

/**
 * 医疗工作流节点枚举
 *
 * @author Chuang
 * created 2025/9/10
 */
@Getter
public enum MedicineNodeEnum {

    INTENT("INTENT", "意图识别节点"),
    MEDICINE("MEDICINE", "药品咨询节点"),
    CONSULT("CONSULT", "健康咨询节点"),
    OTHER("OTHER", "其他问题节点");

    private final String nodeId;
    private final String description;

    MedicineNodeEnum(String nodeId, String description) {
        this.nodeId = nodeId;
        this.description = description;
    }

    @Override
    public String toString() {
        return nodeId;
    }
}
