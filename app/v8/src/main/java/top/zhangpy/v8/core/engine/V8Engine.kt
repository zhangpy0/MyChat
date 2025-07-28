package top.zhangpy.v8.core.engine

import android.util.Log
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8ScriptExecutionException

class V8Engine {
    var v8Runtime: V8? = null
    private var consoleObject: V8Object? = null
    private var outputCallback: ((String) -> Unit)? = null
    private var consoleCallback: ConsoleCallback? = null
    private var jsObjects : MutableMap<String, V8Object> = mutableMapOf()

    init {
        initializeV8()
    }

    private fun initializeV8() {
        try {
            // 创建 V8 运行时实例
            v8Runtime = V8.createV8Runtime()

            // 创建并注入 console 对象
            consoleObject = V8Object(v8Runtime)
            v8Runtime?.add("console", consoleObject)

            // 添加 console.log 方法
            consoleObject?.registerJavaMethod({ receiver, parameters ->
                val output = buildString {
                    for (i in 0 until parameters.length()) {
                        if (i > 0) append(" ")
                        append(parameters.get(i).toString())
                    }
                }
                consoleCallback?.log(output)
            }, "log")

            // 添加 console.error 方法
            consoleObject?.registerJavaMethod({ receiver, parameters ->
                val output = buildString {
                    append("[ERROR] ")
                    for (i in 0 until parameters.length()) {
                        if (i > 0) append(" ")
                        append(parameters.get(i).toString())
                    }
                }
                consoleCallback?.error(output)
            }, "error")

            // 注入基本 API
            injectBasicAPI()

            Log.d("V8Engine", "V8 runtime initialized. Version: ${V8.getV8Version()}")
        } catch (e: Exception) {
            Log.e("V8Engine", "V8 initialization failed", e)
            cleanup()
        }
    }

    private fun injectBasicAPI() {
        try {
            // 注入 setTimeout 和 clearTimeout
            v8Runtime?.executeVoidScript("""
                const __timers = {};
                function setTimeout(callback, delay) {
                    const timerId = Object.keys(__timers).length + 1;
                    __timers[timerId] = true;
                    
                    Thread.start({
                        run: function() {
                            if (__timers[timerId]) {
                                Thread.sleep(delay);
                                if (__timers[timerId]) {
                                    callback();
                                    delete __timers[timerId];
                                }
                            }
                        }
                    });
                    return timerId;
                }
                
                function clearTimeout(timerId) {
                    if (__timers[timerId]) {
                        delete __timers[timerId];
                    }
                }
            """.trimIndent())
        } catch (e: Exception) {
            Log.e("V8Engine", "API injection failed", e)
        }
    }

    fun setOutputCallback(callback: (String) -> Unit) {
        this.outputCallback = callback
    }

    fun setConsoleCallback(callback: ConsoleCallback) {
        this.consoleCallback = callback
    }

    fun execute(code: String): Any {
        return try {
            v8Runtime?.executeScript("try { $code } catch(e) { e.toString(); }") ?: "null"
        } catch (e: V8ScriptExecutionException) {
            Log.e("V8Engine", "Script execution error", e)
            "[JavaScript Error] ${e.message}"
        } catch (e: Exception) {
            Log.e("V8Engine", "Unexpected error", e)
            "[System Error] ${e.message}"
        }
    }

    fun executeAsync(code: String, callback: (Any) -> Unit) {
        Thread {
            val result = execute(code)
            callback(result)
        }.start()
    }

    fun registerJavaMethod(methodName: String, method: (V8Object, V8Array) -> Unit) {
        try {
            v8Runtime?.registerJavaMethod(method, methodName)
        } catch (e: Exception) {
            Log.e("V8Engine", "Failed to register Java method: $methodName", e)
        }
    }

    fun registerJavaObject(name: String, obj: V8Object) {
        try {
            v8Runtime?.add(name, obj)
            jsObjects[name] = obj
        } catch (e: Exception) {
            Log.e("V8Engine", "Failed to register Java object: $name", e)
        }
    }

    fun cleanup() {
        try {
            consoleObject?.close()
            v8Runtime?.release()
            consoleObject = null
            v8Runtime = null
            jsObjects.values.forEach { it.release() }
        } catch (e: Exception) {
            Log.e("V8Engine", "Cleanup failed", e)
        }
    }

    open class ConsoleCallback {
        open fun log(message: String) {
            Log.d("V8Console", message)
        }

        open fun error(message: String) {
            Log.e("V8Console", message)
        }
    }

    companion object {
        init {
            // 加载 J2V8 原生库
            System.loadLibrary("j2v8")
        }
    }
}