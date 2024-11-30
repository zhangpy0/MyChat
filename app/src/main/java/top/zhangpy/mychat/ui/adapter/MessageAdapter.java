package top.zhangpy.mychat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import lombok.Getter;
import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.MessageListItem;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;

    @Getter
    private final List<MessageListItem> messages;

    private String myAvatarPath;

    private String otherAvatarPath;

    public MessageAdapter(Context context, List<MessageListItem> messages, String myAvatarPath, String otherAvatarPath) {
        this.context = context;
        this.messages = messages;
        this.myAvatarPath = myAvatarPath;
        this.otherAvatarPath = otherAvatarPath;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view, myAvatarPath, otherAvatarPath);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageListItem message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        // 对方消息视图
        private final LinearLayout otherMessageContainer;
        private final ImageView ivOtherAvatar;
        private final TextView tvOtherMessage;
        private final ImageView ivOtherImage;

        // 自己消息视图
        private final RelativeLayout myMessageContainer;
        private final ImageView ivMyAvatar;
        private final TextView tvMyMessage;
        private final ImageView ivMyImage;

        private String myAvatarPath;

        private String otherAvatarPath;

        public MessageViewHolder(@NonNull View itemView, String myAvatarPath, String otherAvatarPath) {
            super(itemView);

            // 初始化对方消息视图
            otherMessageContainer = itemView.findViewById(R.id.other_message_container);
            ivOtherAvatar = itemView.findViewById(R.id.iv_other_avatar);
            tvOtherMessage = itemView.findViewById(R.id.tv_other_message);
            ivOtherImage = itemView.findViewById(R.id.iv_other_image);

            // 初始化自己消息视图
            myMessageContainer = itemView.findViewById(R.id.my_message_container);
            ivMyAvatar = itemView.findViewById(R.id.iv_my_avatar);
            tvMyMessage = itemView.findViewById(R.id.tv_my_message);
            ivMyImage = itemView.findViewById(R.id.iv_my_image);

            this.myAvatarPath = myAvatarPath;
            this.otherAvatarPath = otherAvatarPath;
        }

        public void bind(MessageListItem message) {
            if (message.isMe()) {
                // 显示自己的消息
                otherMessageContainer.setVisibility(View.GONE);
                myMessageContainer.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(myAvatarPath)
                        .skipMemoryCache(true) // 跳过内存缓存
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                        .placeholder(R.drawable.default_avatar) // 占位符
                        .error(R.drawable.default_avatar) // 加载失败时的图片
                        .into(ivMyAvatar);

                if ("text".equals(message.getMessageType())) {
                    tvMyMessage.setVisibility(View.VISIBLE);
                    ivMyImage.setVisibility(View.GONE);
                    tvMyMessage.setText(message.getContent());
                } else if ("image".equals(message.getMessageType())) {
                    tvMyMessage.setVisibility(View.GONE);
                    ivMyImage.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(message.getFilePath())
                            .skipMemoryCache(true) // 跳过内存缓存
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                            .placeholder(R.drawable.default_avatar) // 占位符
                            .error(R.drawable.default_avatar) // 加载失败时的图片
                            .into(ivMyImage);
                }
            } else {
                // 显示对方的消息
                myMessageContainer.setVisibility(View.GONE);
                otherMessageContainer.setVisibility(View.VISIBLE);

                Glide.with(itemView.getContext())
                        .load(otherAvatarPath)
                        .skipMemoryCache(true) // 跳过内存缓存
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                        .placeholder(R.drawable.default_avatar) // 占位符
                        .error(R.drawable.default_avatar) // 加载失败时的图片
                        .into(ivOtherAvatar);

                if ("text".equals(message.getMessageType())) {
                    tvOtherMessage.setVisibility(View.VISIBLE);
                    ivOtherImage.setVisibility(View.GONE);
                    tvOtherMessage.setText(message.getContent());
                } else if ("image".equals(message.getMessageType())) {
                    tvOtherMessage.setVisibility(View.GONE);
                    ivOtherImage.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(message.getFilePath())
                            .skipMemoryCache(true) // 跳过内存缓存
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                            .placeholder(R.drawable.default_avatar) // 占位符
                            .error(R.drawable.default_avatar) // 加载失败时的图片
                            .into(ivOtherImage);
                }
            }
        }
    }
}