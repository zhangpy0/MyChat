package top.zhangpy.v8.core.parsing

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import top.zhangpy.v8.model.NativeNode

class HtmlParser {

    private val inlineScripts = mutableListOf<String>() // 存储提取的内联脚本

    /**
     * 解析HTML字符串并生成NativeNode树
     * @param html HTML字符串
     * @return NativeNode树根节点
     */
    fun parseHtml(html: String): NativeNode {
        inlineScripts.clear()
        val doc: Document = Jsoup.parse(html)
        val styleRules = parseStyleRules(doc)
        extractInlineScripts(doc) // 提取内联脚本
        return buildNativeTree(doc.body(), styleRules)
    }

    /**
     * 解析文档中的样式规则
     */
    private fun parseStyleRules(doc: Document): List<StyleRule> {
        val rules = mutableListOf<StyleRule>()
        doc.select("style").forEach { styleElement ->
            val css = styleElement.html()
            parseCssRules(css).forEach { rule ->
                rules.add(rule)
            }
        }
        return rules
    }

    /**
     * 解析CSS规则字符串
     */
    private fun parseCssRules(css: String): List<StyleRule> {
        val rules = mutableListOf<StyleRule>()
        // 移除注释
        val cleanedCss = css.replace("/\\*[\\s\\S]*?\\*/".toRegex(), "")
        // 分割规则
        val ruleStrings = cleanedCss.split("}").map { it.trim() + "}" }.filter { it.length > 1 && it != "}" }

        for (ruleStr in ruleStrings) {
            if (ruleStr.trim().isEmpty()) continue
            val parts = ruleStr.split("\\{".toRegex(), 2)
            if (parts.size < 2) continue

            val selectors = parts[0].trim().split(",\\s*".toRegex())
            val declarations = parts[1].trim().removeSuffix("}").trim()

            val styles = parseStyleDeclarations(declarations)
            for (selector in selectors) {
                if (selector.isNotEmpty()) {
                    rules.add(StyleRule(selector.trim(), styles))
                }
            }
        }
        return rules
    }

    /**
     * 解析样式声明
     */
    private fun parseStyleDeclarations(declarations: String): Map<String, String> {
        val styles = mutableMapOf<String, String>()
        declarations.split(";")
            .filter { it.contains(":") }
            .forEach { decl ->
                val parts = decl.split(":", limit = 2)
                val property = parts[0].trim()
                val value = parts[1].trim()
                styles[property] = value
            }
        return styles
    }

    /**
     * 构建NativeNode树
     */
    private fun buildNativeTree(element: Element, styleRules: List<StyleRule>): NativeNode {
        // 1. 收集匹配的样式规则
        val matchedStyles = mutableMapOf<String, String>()
        for (rule in styleRules) {
            if (elementMatchesSelector(element, rule.selector)) {
                matchedStyles.putAll(rule.styles)
            }
        }

        // 2. 解析内联样式并覆盖匹配的规则
        val inlineStyles = parseStyleDeclarations(element.attr("style"))
        val computedStyle = matchedStyles.toMutableMap().apply {
            putAll(inlineStyles) // 内联样式优先级更高
        }

        // 3. 收集直接文本内容
        val textContent = element.textNodes()
            .joinToString("") { it.text().trim() }
            .takeIf { it.isNotEmpty() }

        // 4. 递归处理子元素
        val children = mutableListOf<NativeNode>()
        element.children().forEach { child ->
            children.add(buildNativeTree(child, styleRules))
        }

        return NativeNode(
            tagName = element.tagName(),
            attributes = element.attributes().associate { it.key to it.value },
            style = computedStyle,
            textContent = textContent,
            children = children
        )
    }

    /**
     * 检查元素是否匹配选择器
     */
    private fun elementMatchesSelector(element: Element, selector: String): Boolean {
        // 简化实现：仅支持基本选择器（标签、类、ID）
        val simpleSelector = selector.split("\\s+".toRegex()).last()

        return when {
            // ID选择器
            simpleSelector.startsWith("#") -> {
                val id = simpleSelector.substring(1)
                element.id() == id
            }
            // 类选择器
            simpleSelector.startsWith(".") -> {
                val className = simpleSelector.substring(1)
                element.hasClass(className)
            }
            // 标签选择器
            else -> {
                element.tagName().equals(simpleSelector, ignoreCase = true)
            }
        }
    }

    /**
     * 提取文档中的内联JavaScript代码
     */
    private fun extractInlineScripts(doc: Document) {
        doc.select("script").forEach { scriptElement ->
            // 只提取没有src属性的内联脚本
            if (!scriptElement.hasAttr("src")) {
                val scriptCode = scriptElement.html().trim()
                if (scriptCode.isNotEmpty()) {
                    inlineScripts.add(scriptCode)
                }
            }
        }
    }

    /**
     * 获取提取的内联JavaScript代码
     * @return 内联JavaScript代码列表
     */
    fun extractScripts(): List<String> {
        return inlineScripts.toList()
    }
}

/**
 * CSS样式规则数据类
 * @property selector CSS选择器
 * @property styles 样式属性Map
 */
private data class StyleRule(val selector: String, val styles: Map<String, String>)