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
        int index = (int) (event.getY() / (getHeight() / letters.length));
        if (index >= 0 && index < letters.length) {
            currentIndex = index;
            if (callback != null) {
                callback.onSelect(letters[index]);
            }
        }
        invalidate();
        return true;
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