package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.util.Logger;

public class SearchFriendActivity extends AppCompatActivity {

    private EditText searchHint;
    private TextView tvCancel;
    private TextView tipsText;
    private LinearLayout searchLayout;
    private LinearLayout tipsLayout;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);

        Logger.initialize(getApplicationContext());
        Logger.enableLogging(true);

        // 初始化组件
        searchHint = findViewById(R.id.search_edit_text);
        tvCancel = findViewById(R.id.tv_cancel);
        tipsText = findViewById(R.id.tips_text);
        searchLayout = findViewById(R.id.search_layout);
        tipsLayout = findViewById(R.id.tips_layout);

        // 设置进入页面时自动聚焦并弹出键盘
        searchHint.requestFocus();

        tipsLayout.setVisibility(View.GONE);

        // 监听输入框内容变化
        searchHint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // 在文字改变前的操作（可选）
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 动态更新提示框文本
                if (charSequence.length() == 0) {
                    tipsText.setText("搜索:");
                    tipsLayout.setVisibility(View.GONE);
                    text = "";
                } else {
                    tipsText.setText("搜索:" + charSequence.toString());
                    tipsLayout.setVisibility(View.VISIBLE);
                    text = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 在文字改变后的操作（可选）
            }
        });

        tipsLayout.setOnClickListener(v -> {
            int contactId = -1;
            boolean isNumber = true;
            try {
                contactId = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                Logger.e("SearchFriendActivity", "NumberFormatException: " + e.getMessage());
                isNumber = false;
                Toast.makeText(SearchFriendActivity.this, "请输入正确的用户ID", Toast.LENGTH_SHORT).show();
            }
            if (isNumber) {
                Intent intent = new Intent(SearchFriendActivity.this, ContactInfoActivity.class);
                intent.putExtra("contact_id", contactId);
                intent.putExtra("is_search", true);
                startActivity(intent);
            }
        });

        // 设置取消按钮功能
        tvCancel.setOnClickListener(v -> {
            finish();
        });
    }
}
