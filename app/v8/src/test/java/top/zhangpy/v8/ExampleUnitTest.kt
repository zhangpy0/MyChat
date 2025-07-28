package top.zhangpy.v8

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test
import top.zhangpy.v8.core.parsing.HtmlParser


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    fun parseHtmlStyles(html: String): Map<String, Map<String, String>> {
        val result: MutableMap<String, Map<String, String>> = HashMap()
        val doc = Jsoup.parse(html)

        // 1. 解析内联样式 (style属性)
        parseInlineStyles(doc, result)

        // 2. 解析嵌入的<style>标签
        parseEmbeddedStyles(doc, result)

        return result
    }

    private fun parseInlineStyles(doc: Document, result: MutableMap<String, Map<String, String>>) {
        // 选择所有带有style属性的元素
        val styledElements = doc.select("[style]")

        for (element in styledElements) {
            val styleAttr = element.attr("style")
            val styles = parseStyleString(styleAttr)


            // 使用元素选择器作为键（简化版）
            val selector = element.tagName() + "#" + element.id() + "." + element.className()
            result[selector] = styles
        }
    }

    private fun parseEmbeddedStyles(
        doc: Document,
        result: MutableMap<String, Map<String, String>>
    ) {
        // 获取所有<style>标签
        val styleTags = doc.select("style")

        for (styleTag in styleTags) {
            val css = styleTag.html()
            // 简化的CSS解析（实际项目需更完整实现）
            for (rule in css.split("}".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (rule.trim { it <= ' ' }.isEmpty()) continue

                val parts =
                    rule.split("\\{".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size < 2) continue

                val selector = parts[0].trim { it <= ' ' }
                val stylesStr = parts[1].trim { it <= ' ' }

                val styles = parseStyleString(stylesStr)
                result[selector] = styles
            }
        }
    }

    private fun parseStyleString(styleString: String): Map<String, String> {
        val styleMap: MutableMap<String, String> = HashMap()
        val declarations =
            styleString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (decl in declarations) {
            val parts = decl.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size == 2) {
                val property = parts[0].trim { it <= ' ' }
                val value = parts[1].trim { it <= ' ' }
                styleMap[property] = value
            }
        }
        return styleMap
    }
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        // app/v8/src/main/assets/test.html
        val html = """<html lang="en">
<head>
    <style>
        p {
            color: #333333;
            font-size: 16px;
        }
        .container {
            display: flex;
            flex-direction: column;
            background-color: #f0f0f0;
        }
    </style>
    <title></title>
</head>
<body>
<div id="main" class="container" style="padding: 10px;">
    <p style="font-weight: bold;">Hello, Native World!</p>
    <img src="https://example.com/image.png" style="width: 100px; height: 100px;"/>
</div>
</body>
</html>""".trimIndent()
        val doc: Document = Jsoup.parse(html)
        val htmlParser = HtmlParser()
        val nativeTree = htmlParser.parseHtml(html)
        println(nativeTree)
    }
}