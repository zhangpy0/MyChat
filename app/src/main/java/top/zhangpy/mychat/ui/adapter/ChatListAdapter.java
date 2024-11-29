package top.zhangpy.mychat.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.ChatListItem;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<ChatListItem> messages = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setMessages(List<ChatListItem> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weixin_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatListItem message = messages.get(position);
        holder.contactName.setText(message.getContactName());
        StringBuilder sb = new StringBuilder();
        if (message.getUnreadCount() > 0) {
            sb.append("[").append(message.getUnreadCount()).append("条] ");
        }
        if (!message.getSenderName().equals(message.getContactName()) && !message.getContent().isEmpty()) {
            sb.append(message.getSenderName()).append(":");
        }
        sb.append(message.getContent());
        holder.content.setText(sb.toString());
        holder.time.setText(message.getTime());

        if (message.getAvatarPath() == null || message.getAvatarPath().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.default_avatar)
                    .into(holder.avatar);
            return;
        }
        Glide.with(holder.itemView.getContext())
                .load(message.getAvatarPath())
                .skipMemoryCache(true) // 跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                .placeholder(R.drawable.default_avatar) // 占位符
                .error(R.drawable.default_avatar) // 加载失败时的图片
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView content, time, unreadCount, senderName, contactName;
        ImageView avatar;

        public ChatViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            time = itemView.findViewById(R.id.time);
            contactName = itemView.findViewById(R.id.contact_name);
            avatar = itemView.findViewById(R.id.img1);
        }
    }
}
