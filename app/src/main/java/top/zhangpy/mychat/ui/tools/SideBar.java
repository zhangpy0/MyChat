package top.zhangpy.mychat.ui.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SideBar extends View {
    private String[] letters = {"A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    private Paint textPaint, bigTextPaint, scaleTextPaint;
    private int currentIndex = -1;
    private ISideBarSelectCallback callback;

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(40); // 可用资源文件动态配置
        textPaint.setTextAlign(Paint.Align.CENTER);

        bigTextPaint = new Paint(textPaint);
        bigTextPaint.setTextSize(60); // 放大字体

        scaleTextPaint = new Paint(textPaint);
        scaleTextPaint.setTextSize(50); // 缩放字体
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int itemHeight = getHeight() / letters.length;

        for (int i = 0; i < letters.length; i++) {
            float x = getWidth() - 100; // 靠右对齐
            float y = itemHeight * (i + 1);

            Paint paint = (i == currentIndex) ? bigTextPaint : textPaint;
            canvas.drawText(letters[i], x, y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 判断触摸事件是否在 SideBar 区域
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float y = event.getY();
            int index = (int) (y / ((float) getHeight() / letters.length));
            if (index >= 0 && index < letters.length) {
                currentIndex = index;
                if (callback != null) {
                    callback.onSelect(letters[index]);
                }
            }
        }

        // 如果手指抬起，清除选中状态
        if (action == MotionEvent.ACTION_UP) {
            currentIndex = -1;
        }

        invalidate();
        return true; // 保留现有行为：SideBar 消耗它关心的事件
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        // 只拦截 SideBar 自己关心的触摸事件
//        return isTouchInside(event) || super.onInterceptTouchEvent(event);
//    }

    private boolean isTouchInside(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        return x >= (getWidth() - 100) && y >= 0 && y <= getHeight(); // 根据绘制区域调整边界
    }


    public void setLetters(String[] letters) {
        this.letters = letters;
        invalidate();
    }

    public void setOnSelectCallback(ISideBarSelectCallback callback) {
        this.callback = callback;
    }

    public interface ISideBarSelectCallback {
        void onSelect(String letter);
    }
}