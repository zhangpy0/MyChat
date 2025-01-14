package top.zhangpy.mychat.ui.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.SendApplyViewModel;

public class SendApplyActivity extends AppCompatActivity {

    private SendApplyViewModel sendApplyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_apply);

        int contactId = getIntent().getIntExtra("contact_id", -1);

        if (contactId == -1) {
            Toast.makeText(this, "联系人不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sendApplyViewModel = new ViewModelProvider(this).get(SendApplyViewModel.class);

        EditText etContent = findViewById(R.id.et_apply_text);
        Button btnSend = findViewById(R.id.btn_send_apply);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnSend.setOnClickListener(v -> {
            String content = etContent.getText().toString();
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入验证消息", Toast.LENGTH_SHORT).show();
                return;
            }

            sendApplyViewModel.sendApply(contactId, content);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
