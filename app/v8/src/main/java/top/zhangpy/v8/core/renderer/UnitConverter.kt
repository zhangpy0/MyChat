package top.zhangpy.v8.core.renderer

// UnitConverter.kt
import android.content.Context
import android.util.TypedValue

object UnitConverter {
    /**
     * 将像素（px）值转换为密度无关像素（dp）。
     */
    fun pxToDp(px: Float, context: Context): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    /**
     * 将密度无关像素（dp）值转换为像素（px）。
     */
    fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    /**
     * 将像素（px）值转换为可缩放像素（sp）。
     */
    fun pxToSp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.scaledDensity
    }
}