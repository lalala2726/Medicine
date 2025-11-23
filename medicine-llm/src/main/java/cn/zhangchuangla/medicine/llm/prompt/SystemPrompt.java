package cn.zhangchuangla.medicine.llm.prompt;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
public class SystemPrompt {


    public static final String DRUG_PARSER_PROMPT = """
            请你严格从用户提供的药品图片中识别信息，并将可识别内容转换为对应字段的 JSON 数据。
            注意要求：
            1. 只能填写图片中真实可见的内容。
            2. 如果某个字段图片中未出现、模糊、缺失，请将该字段设置为 null。
            3. 禁止推测、禁止杜撰、禁止根据药品名称自动补全说明书内容。
            4. 所有填写内容必须严格来自图片。
            5. 输出必须是严格符合 JSON 语法的结构化数据，不允许出现额外解释。
            需要提取的字段如下（字段含义供你参考，不允许用于推测填写）：
            {
              "commonName": "药品通用名，例如‘喉咙清颗粒’",
              "composition": "成分，例如‘土牛膝、马兰草…’",
              "characteristics": "性状，例如‘棕褐色的颗粒；味甜…’",
              "packaging": "包装规格，例如‘12袋/盒’",
              "validityPeriod": "有效期，例如‘24个月’",
              "storageConditions": "贮藏条件，例如‘密封，阴凉处’",
              "productionUnit": "生产单位，例如‘XX药业有限公司’",
              "approvalNumber": "批准文号，例如‘国药准字Z20090802’",
              "executiveStandard": "执行标准，例如‘YBZ13322009’",
              "originType": "国产/进口",
              "isOutpatientMedicine": "是否外用药，布尔值",
              "warmTips": "温馨提示",
              "brand": "品牌名称",
              "prescription": "是否处方药（布尔值）",
              "efficacy": "功能主治",
              "usageMethod": "用法用量",
              "adverseReactions": "不良反应",
              "precautions": "注意事项",
              "taboo": "禁忌",
              "instruction": "药品完整说明书全文（如能完整识别可填写）"
            }
            请根据上面的字段结构，结合图片内容生成 JSON。
            未识别到的字段必须使用 null 填充，禁止生成任何图片中不存在的信息。
            
            请输出最终 JSON：
            """;
}
