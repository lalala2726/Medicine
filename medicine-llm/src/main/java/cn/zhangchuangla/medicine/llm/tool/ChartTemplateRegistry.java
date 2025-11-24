package cn.zhangchuangla.medicine.llm.tool;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 聊天图表模板注册表：按需向大模型暴露支持的图表类型及示例。
 */
public final class ChartTemplateRegistry {

    private static final List<ChartTemplate> TEMPLATES = List.of(
            new ChartTemplate(
                    "折线图",
                    "line",
                    "时间序列趋势对比（单条或多分组）",
                    """
                            {
                              "data": [
                                { "time": "2023-01", "value": 120 },
                                { "time": "2023-02", "value": 150 }
                              ],
                              "axisXTitle": "月份",
                              "axisYTitle": "销售额(万元)",
                              "height": 500,
                              "lineWidth": 2
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 time(横轴) 与 value(纵轴)，可加 group 做多折线",
                            "axisXTitle", "可选，X 轴标题，例如 月份",
                            "axisYTitle", "可选，Y 轴标题，例如 销售额(万元)",
                            "lineWidth", "可选，线宽，默认 2",
                            "height", "可选，高度(px)，推荐 400-600"
                    )
            ),
            new ChartTemplate(
                    "柱状图",
                    "column",
                    "类别对比、排名（纵向或横向）",
                    """
                            {
                              "data": [
                                { "category": "电子产品", "value": 35.5 },
                                { "category": "服饰", "value": 28.3 }
                              ],
                              "axisXTitle": "产品类别",
                              "axisYTitle": "销售占比(%)",
                              "direction": "vertical",
                              "height": 500
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 category(分类) 与 value(数值)，可加 group 做分组对比",
                            "axisXTitle", "可选，X 轴标题，纵向时为横轴标题",
                            "axisYTitle", "可选，Y 轴标题，纵向时为纵轴标题",
                            "direction", "可选，vertical(纵向) 或 horizontal(横向)",
                            "height", "可选，高度(px)，推荐 450-600"
                    )
            ),
            new ChartTemplate(
                    "饼图",
                    "pie",
                    "占比分布、部分与整体关系",
                    """
                            {
                              "data": [
                                { "category": "智能手机", "value": 35 },
                                { "category": "笔记本电脑", "value": 28 }
                              ],
                              "height": 500
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 category(类别) 与 value(占比/数值)",
                            "height", "可选，高度(px)，推荐 400-600",
                            "palette", "可选，颜色数组，对应各扇区"
                    )
            ),
            new ChartTemplate(
                    "双轴图",
                    "dualaxes",
                    "两个量纲对比（混合折线/柱）",
                    """
                            {
                              "categories": ["2024-01", "2024-02", "2024-03"],
                              "series": [
                                { "type": "column", "data": [10800, 8700, 10800], "axisYTitle": "处理时间(s)" },
                                { "type": "line", "data": [649.5, 1053.7, 679.8], "axisYTitle": "完成时间(ms)" }
                              ],
                              "height": 450
                            }
                            """,
                    Map.of(
                            "categories", "必填，数组，X 轴分类标签，如日期",
                            "series", "必填，数组，元素含 type(column|line)、data(数值数组)、axisYTitle(Y 轴标题)",
                            "height", "可选，高度(px)，推荐 400-500"
                    )
            ),
            new ChartTemplate(
                    "雷达图",
                    "radar",
                    "多维度能力/评分对比",
                    """
                            {
                              "data": [
                                { "name": "沟通", "value": 8 },
                                { "name": "协作", "value": 9 },
                                { "name": "技术", "value": 10 }
                              ],
                              "height": 550
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 name(维度) 与 value(评分，常见 0-10)",
                            "height", "可选，高度(px)，推荐 500-600",
                            "palette", "可选，颜色数组"
                    )
            ),
            new ChartTemplate(
                    "面积图",
                    "area",
                    "趋势展示且强调量级",
                    """
                            {
                              "data": [
                                { "time": "2014", "value": 580 },
                                { "time": "2015", "value": 1200 }
                              ],
                              "axisXTitle": "年份",
                              "axisYTitle": "用户数(万)",
                              "height": 500
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 time 与 value，可加 group 做多区域",
                            "axisXTitle", "可选，X 轴标题，例如 年份",
                            "axisYTitle", "可选，Y 轴标题，例如 用户数(万)",
                            "height", "可选，高度(px)，推荐 400-600"
                    )
            ),
            new ChartTemplate(
                    "条形图",
                    "bar",
                    "水平排名/对比",
                    """
                            {
                              "data": [
                                { "category": "上海", "value": 18245 },
                                { "category": "北京", "value": 15680 }
                              ],
                              "axisXTitle": "销售额(万元)",
                              "axisYTitle": "城市",
                              "height": 500
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 category 与 value",
                            "axisXTitle", "可选，X 轴标题（横向为数值轴）",
                            "axisYTitle", "可选，Y 轴标题（横向为类别轴）",
                            "height", "可选，高度(px)，推荐 400-600"
                    )
            ),
            new ChartTemplate(
                    "直方图",
                    "histogram",
                    "数值分布/频次统计",
                    """
                            {
                              "data": [45, 52, 58, 61, 63, 67, 70],
                              "title": "成绩分布",
                              "binNumber": 10,
                              "axisXTitle": "分数区间",
                              "axisYTitle": "人数",
                              "height": 500
                            }
                            """,
                    Map.of(
                            "data", "必填，纯数值数组",
                            "binNumber", "可选，分组数量（柱子数）",
                            "title", "可选，图表标题",
                            "axisXTitle", "可选，X 轴标题，如分数区间",
                            "axisYTitle", "可选，Y 轴标题，如人数"
                    )
            ),
            new ChartTemplate(
                    "散点图",
                    "scatter",
                    "两个变量相关性",
                    """
                            {
                              "data": [
                                { "x": 161.2, "y": 51.6 },
                                { "x": 167.5, "y": 59 }
                              ],
                              "axisXTitle": "身高(cm)",
                              "axisYTitle": "体重(kg)",
                              "height": 450
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 x 与 y",
                            "axisXTitle", "可选，X 轴标题",
                            "axisYTitle", "可选，Y 轴标题",
                            "height", "可选，高度(px)，推荐 400-500"
                    )
            ),
            new ChartTemplate(
                    "词云图",
                    "wordcloud",
                    "关键词频率/权重",
                    """
                            {
                              "data": [
                                { "value": 22.44, "text": "上帝" },
                                { "value": 27.88, "text": "感官" }
                              ],
                              "height": 450
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 value(权重) 与 text(词语)",
                            "height", "可选，高度(px)，推荐 400-500"
                    )
            ),
            new ChartTemplate(
                    "矩阵树图",
                    "treemap",
                    "层级占比对比",
                    """
                            {
                              "data": [
                                { "name": "智能手机", "value": 560 },
                                { "name": "平板电脑", "value": 500 }
                              ],
                              "height": 450
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 name 与 value，面积与值成比例",
                            "height", "可选，高度(px)，推荐 400-500"
                    )
            ),
            new ChartTemplate(
                    "漏斗图",
                    "funnel",
                    "流程转化与流失分析",
                    """
                            {
                              "data": [
                                { "category": "浏览", "value": 50000 },
                                { "category": "下单", "value": 25000 },
                                { "category": "支付", "value": 15000 }
                              ],
                              "height": 350
                            }
                            """,
                    Map.of(
                            "data", "必填，数组，元素含 category(阶段) 与 value(数值)",
                            "height", "可选，高度(px)，推荐 300-400",
                            "palette", "可选，颜色数组"
                    )
            ),
            new ChartTemplate(
                    "思维导图",
                    "mindmap",
                    "层级/脑图展示",
                    """
                            {
                              "data": {
                                "name": "项目计划",
                                "children": [
                                  { "name": "研究阶段" },
                                  { "name": "设计阶段" }
                                ]
                              },
                              "height": 600
                            }
                            """,
                    Map.of(
                            "data", "必填，树结构，节点需包含 name，children 可递归",
                            "height", "可选，高度(px)，推荐 500-600"
                    )
            ),
            new ChartTemplate(
                    "网络图",
                    "networkgraph",
                    "关系网络、人物关系",
                    """
                            {
                              "data": {
                                "nodes": [
                                  { "name": "哈利·波特" },
                                  { "name": "赫敏·格兰杰" }
                                ],
                                "edges": [
                                  { "source": "哈利·波特", "target": "赫敏·格兰杰", "name": "朋友" }
                                ]
                              },
                              "height": 600
                            }
                            """,
                    Map.of(
                            "nodes", "必填，节点数组，元素含 name",
                            "edges", "必填，边数组，元素含 source 与 target，可加 name 作为关系标签",
                            "height", "可选，高度(px)，推荐 500-600"
                    )
            ),
            new ChartTemplate(
                    "流程图",
                    "flowdiagram",
                    "业务流程/决策路径",
                    """
                            {
                              "data": {
                                "nodes": [
                                  { "name": "客户咨询" },
                                  { "name": "判断问题类型" }
                                ],
                                "edges": [
                                  { "source": "客户咨询", "target": "判断问题类型" }
                                ]
                              },
                              "height": 600
                            }
                            """,
                    Map.of(
                            "nodes", "必填，节点数组，元素含 name",
                            "edges", "必填，边数组，元素含 source、target，可选 name",
                            "height", "可选，高度(px)，推荐 500-600"
                    )
            ),
            new ChartTemplate(
                    "组织架构图",
                    "organizationchart",
                    "组织结构展示",
                    """
                            {
                              "data": {
                                "name": "Alice Johnson",
                                "description": "CTO",
                                "children": [
                                  { "name": "Bob Smith", "description": "工程师" }
                                ]
                              },
                              "height": 600
                            }
                            """,
                    Map.of(
                            "name", "必填，节点名称",
                            "description", "可选，节点描述/职位",
                            "children", "可选，下属节点数组",
                            "height", "可选，高度(px)，推荐 500-600"
                    )
            ),
            new ChartTemplate(
                    "缩进树",
                    "indentedtree",
                    "目录/知识树结构",
                    """
                            {
                              "data": {
                                "name": "my-project",
                                "children": [
                                  { "name": "src" },
                                  { "name": "public" }
                                ]
                              },
                              "height": 600
                            }
                            """,
                    Map.of(
                            "data", "必填，树结构，含 name 字段，children 递归",
                            "theme", "可选，default 或 academy",
                            "height", "可选，高度(px)，推荐 500-600"
                    )
            ),
            new ChartTemplate(
                    "鱼骨图",
                    "fishbonediagram",
                    "因果/根因分析",
                    """
                            {
                              "data": {
                                "name": "产品盈利未达标",
                                "children": [
                                  {
                                    "name": "市场因素",
                                    "children": [{ "name": "竞争激烈" }, { "name": "需求下滑" }]
                                  }
                                ]
                              },
                              "height": 600
                            }
                            """,
                    Map.of(
                            "data", "必填，树结构，name 为问题或原因，children 为子原因",
                            "height", "可选，高度(px)，推荐 500-600"
                    )
            )
    );

    private ChartTemplateRegistry() {
    }

    public static List<ChartDescriptor> descriptors() {
        return TEMPLATES.stream()
                .map(t -> new ChartDescriptor(t.name(), t.type(), t.scene()))
                .toList();
    }

    public static Optional<ChartSample> sampleByNameOrType(String nameOrType) {
        if (nameOrType == null) {
            return Optional.empty();
        }
        String keyword = nameOrType.trim().toLowerCase();
        return TEMPLATES.stream()
                .filter(t -> t.type().equalsIgnoreCase(keyword) || t.name().toLowerCase().contains(keyword))
                .findFirst()
                .map(ChartTemplateRegistry::toSample);
    }

    private static ChartSample toSample(ChartTemplate template) {
        String fenced = "```" + template.type() + "\n" + template.sampleJson().trim() + "\n```";
        return new ChartSample(
                template.name(),
                template.type(),
                template.scene(),
                fenced,
                template.fieldComments()
        );
    }

    public record ChartDescriptor(String name, String type, String scene) {
    }

    public record ChartSample(String name,
                              String type,
                              String scene,
                              String messageType,
                              Map<String, String> fieldComments) {
    }

    private record ChartTemplate(String name,
                                 String type,
                                 String scene,
                                 String sampleJson,
                                 Map<String, String> fieldComments) {
    }
}
