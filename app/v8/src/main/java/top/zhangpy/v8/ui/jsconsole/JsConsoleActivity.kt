package top.zhangpy.v8.ui.jsconsole

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import top.zhangpy.v8.core.engine.HtmlOrchestrator

class JsConsoleActivity : AppCompatActivity() {
    private lateinit var jsConsoleView: JsConsoleView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jsConsoleView = JsConsoleView(this)
//        setContentView(jsConsoleView)
        // 移除状态栏自定义颜色（恢复系统默认）
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        Log.d("JsConsoleActivity", "onCreate called")
        val htmlPath = "test.html"
        val htmlFile = assets.open(htmlPath).bufferedReader().use { it.readText() }
        val rootView = FlexboxLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.MATCH_PARENT,
                FlexboxLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(0, 0, 0, 0)
            }
        }
        rootView.fitsSystemWindows = true
        val htmlOrchestrator = HtmlOrchestrator(this, rootView)
        htmlOrchestrator.render(htmlFile, null)
        setContentView(rootView)
    }

    override fun onDestroy() {
        super.onDestroy()
        jsConsoleView.release()
    }
}