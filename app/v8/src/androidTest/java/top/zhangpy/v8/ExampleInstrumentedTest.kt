package top.zhangpy.v8

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Test
import org.junit.runner.RunWith
import top.zhangpy.v8.core.parsing.HtmlParser
import top.zhangpy.v8.core.renderer.NativeViewRenderer

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

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
        val renderer = NativeViewRenderer(appContext)
        val rootView = renderer.render(nativeTree)
        println()
    }
}