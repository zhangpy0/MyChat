package top.zhangpy.v8.core.engine

// DomApi.kt

import com.eclipsesource.v8.JavaCallback
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import top.zhangpy.v8.model.NativeNode

/**
 * 负责提供 document.getElementById() 功能
 */
class JSDocument(private val v8Engine: V8Engine, private val rootNode: NativeNode, private val renderCallback: () -> Unit) {


    /**
     * 在 NativeNode 树中递归查找具有指定ID的节点。
     */
    private fun findNodeById(node: NativeNode, id: String): NativeNode? {
        if (node.attributes["id"] == id) {
            return node
        }
        for (child in node.children) {
            val found = findNodeById(child, id)
            if (found != null) {
                return found
            }
        }
        return null
    }

    /**
     * JS调用的 getElementById 方法。
     * 查找节点并为其创建一个JS代理对象。
     */
    fun getElementById(id: String): V8Object? {
        val foundNode = findNodeById(rootNode, id)
        return foundNode?.let { JSNodeProxy.create(it, v8Engine.v8Runtime, renderCallback) }
    }
}

object JSNodeProxy {
    fun create(node: NativeNode, v8: V8?, renderCallback: () -> Unit): V8Object {
        val v8Node = V8Object(v8)

        // 初始化代理对象的属性
        node.textContent?.let { v8Node.add("textContent", it) } ?: v8Node.addUndefined("textContent")

        // 初始化style对象
        val v8Style = V8Object(v8)
        node.style.forEach { (k, v) -> v8Style.add(k, v) }
        v8Node.add("style", v8Style)
        v8Style.release()

        // 注册updateNode方法
        val updateCallback = JavaCallback { receiver, parameters ->
            // 1. 同步textContent
            if (receiver.contains("textContent")) {
                val value = receiver.get("textContent")
                node.textContent = value?.toString().takeIf {
                    it != null && it != "undefined" && it != "null" && it.isNotEmpty()
                }
            } else {
                node.textContent = null
            }

            // 2. 同步style
            val newStyle = if (receiver.contains("style")) {
                val styleObj = receiver.getObject("style")
                if (!styleObj.isUndefined) {
                    val styleMap = mutableMapOf<String, String>()
                    val keys = styleObj.keys
                    for (i in 0 until keys.size) {
                        val key = keys[i]
                        val styleValue = styleObj.get(key)
                        if (styleValue != null) {
                            styleMap[key] = styleValue.toString()
                        }
                    }
                    styleObj.release()
                    styleMap
                } else {
                    emptyMap()
                }
            } else {
                emptyMap()
            }
            node.style = newStyle
            renderCallback()
        }

        v8Node.registerJavaMethod(updateCallback, "updateNode")
        return v8Node
    }
}