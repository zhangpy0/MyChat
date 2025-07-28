package top.zhangpy.v8.model

/**
 * 一个表示HTML元素的简化、自定义的数据结构。
 * 它存储了渲染原生视图所需的所有信息，包括标签名、属性、文本内容以及最重要的“计算后样式”。
 *
 * @param tagName HTML标签名, e.g., "div", "p", "img".
 * @param attributes 元素的HTML属性Map, e.g., "id", "src", "onclick".
 * @param style 一个Map，存储了所有应用到此节点上的最终CSS属性和值。
 * @param textContent 仅包含此节点直接拥有的文本，不包括其子节点的文本。
 * @param children 一个包含所有子节点的列表。
 */
data class NativeNode(
    val tagName: String,
    val attributes: Map<String, String>,
    var style: Map<String, String>,
    var textContent: String?,
    val children: List<NativeNode>
)
