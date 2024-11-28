package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.EditGenderViewModel;
import top.zhangpy.mychat.ui.viewmodel.PersonalInfoViewModel;

public class EditGenderActivity extends AppCompatActivity {

    public static final String RESULT_GENDER = "result_gender";

    private EditGenderViewModel viewModel;

    private PersonalInfoViewModel fatherViewModel;

    private LinearLayout llMale, llFemale;
    private ImageView ivMaleCheck, ivFemaleCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gender);

        // 初始化组件
        llMale = findViewById(R.id.ll_male);
        llFemale = findViewById(R.id.ll_female);
        ivMaleCheck = findViewById(R.id.iv_male_check);
        ivFemaleCheck = findViewById(R.id.iv_female_check);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(EditGenderViewModel.class);
        viewModel.initGender();

        fatherViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(PersonalInfoViewModel.class);

        // 观察 ViewModel 的数据并更新 UI
        viewModel.getSelectedGender().observe(this, gender -> {
            ivMaleCheck.setVisibility("男".equals(gender) ? View.VISIBLE : View.GONE);
            ivFemaleCheck.setVisibility("女".equals(gender) ? View.VISIBLE : View.GONE);
        });

        // 点击男
        llMale.setOnClickListener(v -> viewModel.setSelectedGender("男"));

        // 点击女
        llFemale.setOnClickListener(v -> viewModel.setSelectedGender("女"));

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 保存按钮
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_GENDER, viewModel.getSelectedGender().getValue());
            setResult(RESULT_OK, resultIntent);
            viewModel.updateGenderToLocalAndServer();
//            fatherViewModel.updateUserInfoFromLocal();
            finish();
        });
    }
}
