package top.zhangpy.mychat.util;

import android.app.Activity;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class DoubleSwipeBackHelper {
    private final Activity activity;
    private GestureDetector gestureDetector;
    private int swipeBackCount = 0;
    private long firstSwipeTime = 0;

    // 可配置参数
    private int edgeMargin = 50; // 单位：dp
    private int swipeThreshold = 100;
    private int velocityThreshold = 100;
    private int timeout = 2000; // 单位：ms
    private String promptText = "再滑动一次退出应用";

    public DoubleSwipeBackHelper(Activity activity) {
        this.activity = activity;
        initGestureDetector();
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return handleFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    private boolean handleFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) return false;

        final int edgePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                edgeMargin,
                activity.getResources().getDisplayMetrics()
        );

        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();

        if (Math.abs(deltaX) > Math.abs(deltaY)
                && Math.abs(deltaX) > swipeThreshold
                && Math.abs(velocityX) > velocityThreshold) {

            if (deltaX > 0 && e1.getX() <= edgePx) {
                handleSwipeBack();
                return true;
            }
        }
        return false;
    }

    private void handleSwipeBack() {
        long currentTime = System.currentTimeMillis();

        if (swipeBackCount == 0 || (currentTime - firstSwipeTime) > timeout) {
            swipeBackCount = 1;
            firstSwipeTime = currentTime;
            showPrompt();
        } else {
            activity.finish();
        }
    }

    private void showPrompt() {
        Toast.makeText(activity, promptText, Toast.LENGTH_SHORT).show();
    }

    // 以下是配置方法
    public DoubleSwipeBackHelper setEdgeMarginDp(int dp) {
        this.edgeMargin = dp;
        return this;
    }

    public DoubleSwipeBackHelper setSwipeTimeout(int milliseconds) {
        this.timeout = milliseconds;
        return this;
    }

    public DoubleSwipeBackHelper setPromptText(String text) {
        this.promptText = text;
        return this;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}
