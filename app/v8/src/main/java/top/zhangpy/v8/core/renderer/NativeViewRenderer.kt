package top.zhangpy.v8.core.renderer

// NativeViewRenderer.kt

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide
import com.google.android.flexbox.AlignContent
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import top.zhangpy.v8.model.NativeNode
import kotlin.math.roundToInt

class NativeViewRenderer(private val context: Context,
                         private val viewIdMap: MutableMap<String, View>, // 用于存储 id -> View 的映射
                         private val onJsEvent: (String) -> Unit // 用于将JS事件回调给执行器
) {

    /**
     * 渲染的入口函数。
     *
     * @param node NativeNode 树的根节点。
     * @return 一个可以被添加到Activity布局中的原生Android View。
     */
    fun render(node: NativeNode): View {
        // 1. 根据节点类型创建对应的View
        val view = createViewForNode(node)

        // 如果节点有ID，则存入map中
        node.attributes["id"]?.let { id ->
            viewIdMap[id] = view
        }

        // 如果节点有onclick事件，设置原生监听器
        node.attributes["onclick"]?.let { jsCode ->
            view.isClickable = true
            view.setOnClickListener {
                onJsEvent(jsCode) // 触发JS事件
            }
        }

        // 2. 将计算后的样式应用到View上
        applyStyles(view, node)

        // 3. 如果View是ViewGroup，则递归渲染其子节点
        if (view is ViewGroup) {
            for (childNode in node.children) {
                val childView = render(childNode) // 递归调用
                view.addView(childView)
            }
        }
        return view
    }

    /**
     * 根据 NativeNode 的 tagName 创建具体的 Android View 实例。
     */
    private fun createViewForNode(node: NativeNode): View {
        return when (node.tagName) {
            "body", "div" -> FlexboxLayout(context)
            "p" -> TextView(context)
            "img" -> ImageView(context)
            else -> {
                // 对于不支持的标签，创建一个空的View作为占位符
                Log.w("Renderer", "Unsupported tag: ${node.tagName}. Creating a placeholder View.")
                View(context)
            }
        }
    }

    /**
     * 将 NativeNode 中存储的 style 应用到 Android View 上。
     */
    private fun applyStyles(view: View, node: NativeNode) {
        val style = node.style

        // 应用布局参数 (width, height, margin)
        val layoutParams = view.layoutParams ?: FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT
        )
        applyLayoutParams(layoutParams as ViewGroup.MarginLayoutParams, style)
        if (node.tagName == "body") {
            layoutParams.height = FlexboxLayout.LayoutParams.MATCH_PARENT
            layoutParams.width = FlexboxLayout.LayoutParams.MATCH_PARENT
        }
        view.layoutParams = layoutParams


        // 应用内边距 (padding)
        val paddingLeft = style["padding-left"]?.removeSuffix("px")?.toFloatOrNull() ?: style["padding"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        val paddingTop = style["padding-top"]?.removeSuffix("px")?.toFloatOrNull() ?: style["padding"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        val paddingRight = style["padding-right"]?.removeSuffix("px")?.toFloatOrNull() ?: style["padding"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        val paddingBottom = style["padding-bottom"]?.removeSuffix("px")?.toFloatOrNull() ?: style["padding"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        view.setPadding(
            UnitConverter.dpToPx(paddingLeft, context),
            UnitConverter.dpToPx(paddingTop, context),
            UnitConverter.dpToPx(paddingRight, context),
            UnitConverter.dpToPx(paddingBottom, context)
        )

        // 应用通用样式 #RRGGBB #AARRGGBB
        style["background-color"]?.let { view.setBackgroundColor(it.toColorInt()) }
        style["opacity"]?.toFloatOrNull()?.let { view.alpha = it }

        // 应用特定View的样式
        when (view) {
            is FlexboxLayout -> {
                style["flex-direction"]?.let {
                    view.flexDirection = when (it) {
                        "column" -> FlexDirection.COLUMN
                        "row" -> FlexDirection.ROW
                        else -> FlexDirection.ROW
                    }
                }
                style["flex-wrap"]?.let { view.flexWrap = if (it == "wrap") FlexWrap.WRAP else FlexWrap.NOWRAP }
                style["justify-content"]?.let {
                    view.justifyContent = when (it) {
                        "flex-end" -> JustifyContent.FLEX_END
                        "center" -> JustifyContent.CENTER
                        "space-between" -> JustifyContent.SPACE_BETWEEN
                        "space-around" -> JustifyContent.SPACE_AROUND
                        else -> JustifyContent.FLEX_START
                    }
                }
            }
            is TextView -> {
                view.text = node.textContent
                style["color"]?.let { view.setTextColor(it.toColorInt()) }
                style["font-size"]?.removeSuffix("px")?.toFloatOrNull()?.let { view.textSize = UnitConverter.pxToSp(it, context) }
                if (style["font-weight"] == "bold") {
                    view.setTypeface(view.typeface, Typeface.BOLD)
                }
                view.gravity = when (style["text-align"]) {
                    "center" -> Gravity.CENTER
                    "right" -> Gravity.END
                    else -> Gravity.START
                }
            }
            is ImageView -> {
                // 使用Glide加载图片
                node.attributes["src"]?.let {
                    Glide.with(context).load(it).into(view)
                }
            }
        }
    }

    private fun applyLayoutParams(params: ViewGroup.MarginLayoutParams, style: Map<String, String>) {
        // 解析 width 和 height
        params.width = style["width"]?.removeSuffix("px")?.toFloatOrNull()
            ?.let { UnitConverter.dpToPx(it, context) } ?: ViewGroup.LayoutParams.WRAP_CONTENT

        params.height = style["height"]?.removeSuffix("px")?.toFloatOrNull()
            ?.let { UnitConverter.dpToPx(it, context) } ?: ViewGroup.LayoutParams.WRAP_CONTENT

        // 解析 margin
        val marginLeft = style["margin-left"]?.removeSuffix("px")?.toFloatOrNull() ?: style["margin"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        val marginTop = style["margin-top"]?.removeSuffix("px")?.toFloatOrNull() ?: style["margin"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        val marginRight = style["margin-right"]?.removeSuffix("px")?.toFloatOrNull() ?: style["margin"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        val marginBottom = style["margin-bottom"]?.removeSuffix("px")?.toFloatOrNull() ?: style["margin"]?.removeSuffix("px")?.toFloatOrNull() ?: 0f
        params.setMargins(
            UnitConverter.dpToPx(marginLeft, context),
            UnitConverter.dpToPx(marginTop, context),
            UnitConverter.dpToPx(marginRight, context),
            UnitConverter.dpToPx(marginBottom, context)
        )
    }

    /**
     * 创建默认布局参数
     */
    private fun createDefaultLayoutParams(view: View, node: NativeNode): ViewGroup.LayoutParams {
        return if (view is FlexboxLayout) {
            FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * 应用内边距
     */
    private fun applyPadding(view: View, style: Map<String, String>) {
        val paddingLeft = style["padding-left"]?.parseDimension(context) ?: 0
        val paddingTop = style["padding-top"]?.parseDimension(context) ?: 0
        val paddingRight = style["padding-right"]?.parseDimension(context) ?: 0
        val paddingBottom = style["padding-bottom"]?.parseDimension(context) ?: 0

        // 简写padding
        style["padding"]?.split(" ")?.let { parts ->
            when (parts.size) {
                1 -> {
                    val value = parts[0].parseDimension(context)
                    view.setPadding(value, value, value, value)
                    return
                }
                2 -> {
                    val vertical = parts[0].parseDimension(context)
                    val horizontal = parts[1].parseDimension(context)
                    view.setPadding(horizontal, vertical, horizontal, vertical)
                    return
                }
                4 -> {
                    view.setPadding(
                        parts[3].parseDimension(context), // left
                        parts[0].parseDimension(context), // top
                        parts[1].parseDimension(context), // right
                        parts[2].parseDimension(context)  // bottom
                    )
                    return
                }

                else -> {}
            }
        }

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    /**
     * 应用边框
     */
    private fun applyBorders(view: View, style: Map<String, String>) {
        // 实现边框需要自定义Drawable，这里简化为背景色变化
        style["border"]?.let { border ->
            val parts = border.split(" ")
            if (parts.size >= 3) {
                val width = parts[0].parseDimension(context)
                val color = parts[2].toColorInt()
                view.setBackgroundColor(color)
            }
        }
    }

    /**
     * 应用Flexbox样式
     */
    private fun applyFlexboxStyles(layout: FlexboxLayout, style: Map<String, String>) {
        // 布局方向
        style["flex-direction"]?.let {
            layout.flexDirection = when (it) {
                "column" -> FlexDirection.COLUMN
                "column-reverse" -> FlexDirection.COLUMN_REVERSE
                "row" -> FlexDirection.ROW
                "row-reverse" -> FlexDirection.ROW_REVERSE
                else -> FlexDirection.ROW
            }
        }

        // 换行
        style["flex-wrap"]?.let {
            layout.flexWrap = when (it) {
                "wrap" -> FlexWrap.WRAP
                "wrap-reverse" -> FlexWrap.WRAP_REVERSE
                "nowrap" -> FlexWrap.NOWRAP
                else -> FlexWrap.NOWRAP
            }
        }

        // 主轴对齐
        style["justify-content"]?.let {
            layout.justifyContent = when (it) {
                "flex-end" -> JustifyContent.FLEX_END
                "center" -> JustifyContent.CENTER
                "space-between" -> JustifyContent.SPACE_BETWEEN
                "space-around" -> JustifyContent.SPACE_AROUND
                "space-evenly" -> JustifyContent.SPACE_EVENLY
                else -> JustifyContent.FLEX_START
            }
        }

        // 交叉轴对齐
        style["align-items"]?.let {
            layout.alignItems = when (it) {
                "flex-start" -> AlignItems.FLEX_START
                "flex-end" -> AlignItems.FLEX_END
                "center" -> AlignItems.CENTER
                "baseline" -> AlignItems.BASELINE
                "stretch" -> AlignItems.STRETCH
                else -> AlignItems.STRETCH
            }
        }

        // 交叉轴内容对齐
        style["align-content"]?.let {
            layout.alignContent = when (it) {
                "flex-start" -> AlignContent.FLEX_START
                "flex-end" -> AlignContent.FLEX_END
                "center" -> AlignContent.CENTER
                "space-between" -> AlignContent.SPACE_BETWEEN
                "space-around" -> AlignContent.SPACE_AROUND
                "stretch" -> AlignContent.STRETCH
                else -> AlignContent.STRETCH
            }
        }
    }

    /**
     * 应用文本样式
     */
    private fun applyTextStyles(textView: TextView, node: NativeNode, style: Map<String, String>) {
        // 设置文本内容
        textView.text = node.textContent ?: ""

        // 文本颜色
        style["color"]?.let {
            try {
                textView.setTextColor(it.toColorInt())
            } catch (e: Exception) {
                Log.e("Renderer", "Invalid text color: $it", e)
            }
        }

        // 字体大小
        style["font-size"]?.parseDimension(context)?.let { size ->
            textView.textSize = size.toSp(context)
        }

        // 字体粗细
        style["font-weight"]?.let {
            val typeface = when {
                it.equals("bold", ignoreCase = true) -> Typeface.DEFAULT_BOLD
                it.equals("light", ignoreCase = true) -> Typeface.DEFAULT
                else -> Typeface.DEFAULT
            }
            textView.typeface = typeface
        }

        // 文本对齐
        style["text-align"]?.let {
            textView.gravity = when (it) {
                "center" -> Gravity.CENTER
                "right" -> Gravity.END or Gravity.CENTER_VERTICAL
                "justify" -> Gravity.START or Gravity.CENTER_VERTICAL
                else -> Gravity.START or Gravity.CENTER_VERTICAL
            }
        }

        // 行高
        style["line-height"]?.parseDimension(context)?.let { lineHeight ->
            textView.setLineSpacing(lineHeight - textView.textSize, 1f)
        }
    }

    /**
     * 应用图片样式
     */
    private fun applyImageStyles(imageView: ImageView, node: NativeNode) {
        // 加载图片
        node.attributes["src"]?.let { src ->
            try {
                Glide.with(context)
                    .load(src)
                    .into(imageView)
            } catch (e: Exception) {
                Log.e("Renderer", "Failed to load image: $src", e)
            }
        }

        // 设置缩放类型
        imageView.scaleType = when (node.style["object-fit"]) {
            "cover" -> ImageView.ScaleType.CENTER_CROP
            "contain" -> ImageView.ScaleType.FIT_CENTER
            else -> ImageView.ScaleType.FIT_CENTER
        }
    }
}


/**
 * 扩展函数：解析CSS尺寸值
 */
private fun String?.parseDimension(context: Context): Int {
    if (this == null) return 0
    return when {
        endsWith("px") -> removeSuffix("px").toFloatOrNull()?.roundToInt() ?: 0
        endsWith("dp") -> removeSuffix("dp").toFloatOrNull()?.toDp(context) ?: 0
        endsWith("sp") -> removeSuffix("sp").toFloatOrNull()?.toSp(context)?.toInt() ?: 0
        this == "auto" -> ViewGroup.LayoutParams.WRAP_CONTENT
        this == "match_parent" || this == "100%" -> ViewGroup.LayoutParams.MATCH_PARENT
        else -> toFloatOrNull()?.roundToInt() ?: 0
    }
}

/**
 * 扩展函数：dp转px
 */
private fun Float.toDp(context: Context): Int {
    return (this * context.resources.displayMetrics.density).roundToInt()
}

/**
 * 扩展函数：sp转px
 */
private fun Float.toSp(context: Context): Float {
    return this * context.resources.displayMetrics.scaledDensity
}

/**
 * 扩展函数：px转sp
 */
private fun Int.toSp(context: Context): Float {
    return this / context.resources.displayMetrics.scaledDensity
}
