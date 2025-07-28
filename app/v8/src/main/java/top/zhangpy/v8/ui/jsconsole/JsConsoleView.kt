package top.zhangpy.v8.ui.jsconsole

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import top.zhangpy.v8.R
import top.zhangpy.v8.core.engine.V8Engine


class JsConsoleView(context: Context) : FrameLayout(context) {
    private val v8Engine = V8Engine()
    private var outputString: Editable = Editable.Factory.getInstance().newEditable("")

    private lateinit var statusBarSpacer : View
    private lateinit var codeContainer: View
    private lateinit var consoleContainer: View
    private lateinit var lineNumbers: TextView
    private lateinit var codeEditor: EditText
    private lateinit var consoleLineNumbers: TextView
    private lateinit var consoleOutput: TextView
    private lateinit var runButton: View
    private val consoleCallback = object : V8Engine.ConsoleCallback() {
        override fun log(message: String) {
            post {
                outputString.append(message).append("\n")
                (consoleOutput as TextView).text = outputString
            }
        }

        override fun error(message: String) {
            post {
                outputString.append("[ERROR] ").append(message).append("\n")
                (consoleOutput as TextView).text = outputString
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.js_exe_layout, this, true)
        statusBarSpacer = findViewById(R.id.statusBarSpacer)
        codeContainer = findViewById(R.id.codeContainer)
        consoleContainer = findViewById(R.id.consoleContainer)
        lineNumbers = findViewById(R.id.lineNumbers)
        codeEditor = findViewById(R.id.codeEditor)
        consoleLineNumbers = findViewById(R.id.consoleLineNumbers)
        consoleOutput = findViewById(R.id.consoleOutput)
        runButton = findViewById(R.id.runButton)
        init()
    }

    private fun init() {
        v8Engine.setConsoleCallback(consoleCallback)
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadius = 20f
        gradientDrawable.setColor(0xFF33FD00.toInt())
        runButton.background = gradientDrawable

        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
        statusBarSpacer.layoutParams.height = 0
        codeEditor.layoutParams.width = (screenWidth * 0.8).toInt()
        codeContainer.layoutParams.height = (screenHeight * 0.5).toInt()
        consoleOutput.layoutParams.width = (screenWidth * 0.8).toInt()
        consoleOutput.layoutParams.height = (screenHeight * 0.5).toInt()

        bindUI()
        registerClear()
    }

    private fun bindUI() {
        // 代码编辑后的行号更新
        (codeEditor as EditText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                (lineNumbers as TextView).text = getLineNumbersString(s.toString())
            }
        })

        // 同步代码编辑区与行号滚动
        codeEditor.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
            lineNumbers.scrollTo(scrollX, scrollY)
        }

        // 同步控制台输出与行号滚动
        consoleOutput.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
            consoleLineNumbers.scrollTo(scrollX, scrollY)
        }

        // 设置运行按钮点击事件
        runButton.setOnClickListener {
            val code = (codeEditor as EditText).text.toString()
            val result = v8Engine.execute(code)
            outputString.append(result.toString()).append("\n")
            (consoleOutput as TextView).text = outputString
            (consoleLineNumbers as TextView).text = getLineNumbersString(outputString.toString())
        }
    }

    private fun getLineNumbersString(text: String): String {
        val lineCount = 1.coerceAtLeast(text.count { it == '\n' } + 1)
        return (1..lineCount).joinToString("\n")
    }

    private fun consoleClear() {
        outputString.clear()
        outputString.append("Console cleared.\n")
        (consoleOutput as TextView).text = outputString
        (consoleLineNumbers as TextView).text = getLineNumbersString(outputString.toString())
    }

    private fun registerClear() {
        v8Engine.registerJavaMethod("clear") { _, _ ->
            consoleClear()
        }
    }

    fun release() {
        v8Engine.cleanup()
    }
}