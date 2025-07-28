package top.zhangpy.v8.core.engine

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.view.ViewGroup
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import top.zhangpy.v8.core.parsing.HtmlParser
import top.zhangpy.v8.core.renderer.NativeViewRenderer
import top.zhangpy.v8.model.NativeNode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class HtmlOrchestrator(private val context: Context, private val container: ViewGroup) {

    private val v8Thread = HandlerThread("V8Thread").apply { start() }
    private val v8Handler = Handler(v8Thread.looper)
    private lateinit var v8Engine : V8Engine

    // 使用原子引用确保线程安全
    private val rootNode = AtomicReference<NativeNode?>()
    private val viewIdMap = ConcurrentHashMap<String, View>()

    private val renderer = NativeViewRenderer(context, viewIdMap) { jsCode ->
        v8Handler.post { v8Engine.execute(jsCode) }
    }

    // 确保所有V8操作在同一个线程
    private val v8Runtime: V8?
        get() = v8Engine.v8Runtime

    init {
        // 确保V8引擎在V8Thread线程初始化
        v8Handler.post {
            v8Engine = V8Engine()
            v8Engine.setConsoleCallback(V8Engine.ConsoleCallback())
        }
    }

    fun render(html: String, jsData: String?) {
        v8Handler.post {
            try {
                jsData?.let { v8Engine.execute("var viewData = $it;") }

                val parser = HtmlParser()
                rootNode.set(parser.parseHtml(html))
                parser.extractScripts().forEach { script ->
                    v8Engine.execute(script)
                }

                registerDomApi(rootNode.get()!!)
                triggerRenderOnMainThread()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun triggerRenderOnMainThread() {
        Handler(context.mainLooper).post {
            val nodeToRender = rootNode.get() ?: return@post
            viewIdMap.clear()
            container.removeAllViews()
            container.addView(renderer.render(nodeToRender))
        }
    }

    private fun registerDomApi(rootNode: NativeNode) {
        v8Handler.post {
            try {
                // 创建渲染回调
                val renderCallback: () -> Unit = { triggerRenderOnMainThread() }
                // 确保在V8Thread线程创建和注册对象
                val jsDocument = JSDocument(v8Engine, rootNode, renderCallback)
                val v8Document = V8Object(v8Runtime).apply {
                    registerJavaMethod(jsDocument, "getElementById", "getElementById", arrayOf(String::class.java))
                }

                v8Engine.registerJavaObject("document", v8Document)
                v8Document.release() // 及时释放资源

                // 注册updateView方法
                v8Engine.registerJavaMethod("updateView") { _, _ ->
                    // 接到JS的重绘指令后，在主线程上使用已修改的node树重新渲染
                    triggerRenderOnMainThread()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun destroy() {
        v8Handler.post {
            v8Engine.cleanup()
            v8Thread.quitSafely()
        }
    }
}