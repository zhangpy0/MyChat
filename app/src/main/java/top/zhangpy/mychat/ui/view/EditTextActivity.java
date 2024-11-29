package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.EditTextViewModel;
import top.zhangpy.mychat.ui.viewmodel.PersonalInfoViewModel;

public class EditTextActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_INITIAL_TEXT = "extra_initial_text";
    public static final String RESULT_TEXT = "result_text";

    private EditTextViewModel viewModel;

    private PersonalInfoViewModel fatherViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);

        // 获取布局组件
        TextView tvTitle = findViewById(R.id.tv_title);
        EditText etInput = findViewById(R.id.et_input);
        ImageButton btnBack = findViewById(R.id.btn_back);
        Button btnSave = findViewById(R.id.btn_save);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(EditTextViewModel.class);

        fatherViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(PersonalInfoViewModel.class);

        // 从 Intent 获取标题和初始内容
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String initialText = getIntent().getStringExtra(EXTRA_INITIAL_TEXT);

        // 设置标题和初始内容
        tvTitle.setText(title != null ? title : "更改内容");
        viewModel.setText(initialText != null ? initialText : "");
        if (title == null) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        int key;
        if (title.equals("更改昵称")) {
            viewModel.initText(1);
            key = 1;
        } else if (title.equals("更改地区")) {
            viewModel.initText(2);
            key = 2;
        } else {
            key = 0;
        }

        // 观察 ViewModel 数据并更新 UI
        viewModel.getText().observe(this, etInput::setText);

        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            String updatedText = etInput.getText().toString().trim();
            viewModel.setText(updatedText);
            if (updatedText.isEmpty()) {
                Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.updateToLocalAndServer(key);
//                fatherViewModel.updateUserInfoFromLocalAndServer();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result_key", key);
                if (key == 1) {
                    resultIntent.putExtra(RESULT_TEXT, updatedText);
                } else if (key == 2) {
                    resultIntent.putExtra(RESULT_TEXT, updatedText);
                }
                setResult(RESULT_OK, resultIntent);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateResult().observe(this, saveResult -> {
            if (saveResult) {
                finish();
            }
        });
    }
}