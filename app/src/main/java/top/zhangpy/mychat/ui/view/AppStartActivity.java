package top.zhangpy.mychat.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import top.zhangpy.mychat.R;
import top.zhangpy.v8.ui.jsconsole.JsConsoleActivity;

public class AppStartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_start);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AppStartActivity.this, JsConsoleActivity.class);
                startActivity(intent);
                finish();
            }
        }, 200);
    }
}
