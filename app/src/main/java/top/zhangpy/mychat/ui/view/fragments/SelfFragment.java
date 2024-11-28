package top.zhangpy.mychat.ui.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.databinding.FragmentSelfBinding;
import top.zhangpy.mychat.ui.view.PersonalInfoActivity;
import top.zhangpy.mychat.ui.view.SettingsActivity;
import top.zhangpy.mychat.ui.viewmodel.SelfViewModel;

public class SelfFragment extends Fragment {

    private FragmentSelfBinding binding; // ViewBinding
    private SelfViewModel selfViewModel; // ViewModel

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 初始化 ViewBinding
        binding = FragmentSelfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 ViewModel
        selfViewModel = new SelfViewModel(requireActivity().getApplication());


        // 绑定 ViewModel 数据到 UI
        bindViewModel();

        // 设置点击事件
        binding.llPersonalInfo.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "个人信息点击", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), PersonalInfoActivity.class);
            startActivity(intent);

        });

        binding.llSettings.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "设置点击", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        selfViewModel.updateSelfInfoFromLocal();
    }

    private void bindViewModel() {
        // 观察 ViewModel 数据并更新 UI
        selfViewModel.getNickname().observe(getViewLifecycleOwner(), nickname -> {
            binding.tvNickname.setText(nickname);
        });

        selfViewModel.getAccount().observe(getViewLifecycleOwner(), account -> {
            binding.tvAccount.setText(account);
        });
        selfViewModel.getAvatar().observe(getViewLifecycleOwner(), avatar -> {
            if (avatar == null || avatar.isEmpty()) {
                Glide.with(this)
                        .load(R.drawable.default_avatar)
                        .into(binding.ivAvatar);
                return;
            }
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.default_avatar) // 占位符
                    .error(R.drawable.default_avatar) // 加载失败时的图片
                    .into(binding.ivAvatar);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 释放 ViewBinding
    }
}
