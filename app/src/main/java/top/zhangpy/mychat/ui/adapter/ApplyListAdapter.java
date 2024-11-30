package top.zhangpy.mychat.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.ApplyListItem;
import top.zhangpy.mychat.ui.viewmodel.AddFriendViewModel;

public class ApplyListAdapter extends RecyclerView.Adapter<ApplyListAdapter.ApplyViewHolder> {

    private List<ApplyListItem> applyListItems = new ArrayList<>();

    private AddFriendViewModel viewModel;

    public ApplyListAdapter(AddFriendViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setApplyListItems(List<ApplyListItem> applyListItems) {
        this.applyListItems = applyListItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apply_item, parent, false);
        return new ApplyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (applyListItems == null) {
            return 0;
        }
        return applyListItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ApplyViewHolder holder, int position) {
        ApplyListItem applyListItem = applyListItems.get(position);
        applyListItem.reset();
        holder.contactName.setText(applyListItem.getContactName());
        holder.content.setText(applyListItem.getContent());
        holder.status.setText(applyListItem.getStatus());
        if (applyListItem.getFlag() != 1 && applyListItem.getStatus().equals("未处理")) {
            holder.status.setVisibility(View.GONE);
            holder.accept.setVisibility(View.VISIBLE);
            holder.reject.setVisibility(View.VISIBLE);
        } else {
            holder.status.setVisibility(View.VISIBLE);
            holder.accept.setVisibility(View.GONE);
            holder.reject.setVisibility(View.GONE);
        }

        holder.accept.setOnClickListener(v -> {
            // TODO: accept apply
            if (applyListItem.getType().equals("friend")) {
                viewModel.processFriendApply(applyListItem.getId(), 1);
            } else {
                viewModel.processGroupApply(applyListItem.getId(), 1);
            }
            applyListItem.setStatus("已添加");
//            holder.status.setText("已添加");
            notifyItemChanged(position);
        });

        holder.reject.setOnClickListener(v -> {
            // TODO: reject apply
            if (applyListItem.getType().equals("friend")) {
                viewModel.processFriendApply(applyListItem.getId(), 0);
            } else {
                viewModel.processGroupApply(applyListItem.getId(), 0);
            }
            applyListItem.setStatus("已拒绝");
            notifyItemChanged(position);
        });

        if (applyListItem.getAvatarPath() == null || applyListItem.getAvatarPath().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.default_avatar)
                    .into(holder.avatar);
            return;
        }

        Glide.with(holder.itemView.getContext())
                .load(applyListItem.getAvatarPath())
                .skipMemoryCache(true) // 跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(holder.avatar);
    }


    public ApplyListItem getItem(int position) {
        return applyListItems.get(position);
    }

    public void updateItemStatus(int applyId) {
        int position = -1;
        for (int i = 0; i < applyListItems.size(); i++) {
            if (applyListItems.get(i).getId() == applyId) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            notifyItemChanged(position); // 刷新特定项
        }
    }

    public static class ApplyViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView contactName;
        TextView content;

        TextView status;

        Button accept;
        Button reject;

        public ApplyViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.contact_img);
            contactName = itemView.findViewById(R.id.contact_name);
            content = itemView.findViewById(R.id.content);
            status = itemView.findViewById(R.id.status_flag);
            accept = itemView.findViewById(R.id.add_flag);
            reject = itemView.findViewById(R.id.reject_flag);
        }
    }
}
