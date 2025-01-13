package top.zhangpy.mychat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.MessageListItem;
import top.zhangpy.mychat.ui.view.ContactInfoActivity;
import top.zhangpy.mychat.ui.view.ImageViewActivity;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;

    @Getter
    private final List<MessageListItem> messages;

    @Setter
    private String myAvatarPath;

    @Setter
    private String otherAvatarPath;

    @Getter
    private int contactId;

    public MessageAdapter(Context context, List<MessageListItem> messages, String myAvatarPath, String otherAvatarPath, int contactId) {
        this.context = context;
        this.messages = messages;
        if (myAvatarPath == null) {
            myAvatarPath = "";
        }
        if (otherAvatarPath == null) {
            otherAvatarPath = "";
        }
        this.myAvatarPath = myAvatarPath;
        this.otherAvatarPath = otherAvatarPath;
        this.contactId = contactId;
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
        private final LinearLayout otherMessageContent;
        private final ImageView ivOtherAvatar;
        private final TextView tvOtherMessage;
        private final ImageView ivOtherImage;

        // 自己消息视图
        private final RelativeLayout myMessageContainer;
        private final LinearLayout myMessageContent;
        private final ImageView ivMyAvatar;
        private final TextView tvMyMessage;
        private final ImageView ivMyImage;

        private String myAvatarPath;

        private String otherAvatarPath;

        public MessageViewHolder(@NonNull View itemView, String myAvatarPath, String otherAvatarPath) {
            super(itemView);

            // 初始化对方消息视图
            otherMessageContainer = itemView.findViewById(R.id.other_message_container);
            otherMessageContent = itemView.findViewById(R.id.other_message_content);
            ivOtherAvatar = itemView.findViewById(R.id.iv_other_avatar);
            tvOtherMessage = itemView.findViewById(R.id.tv_other_message);
            ivOtherImage = itemView.findViewById(R.id.iv_other_image);

            // 初始化自己消息视图
            myMessageContainer = itemView.findViewById(R.id.my_message_container);
            myMessageContent = itemView.findViewById(R.id.my_message_content);
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
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    // 加载失败时处理
                                    return false; // 返回 false 继续让 Glide 显示 error 图片
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    // 图片加载成功后调整 ImageView 高度
                                    if (resource != null) {
                                        int imageWidth = resource.getIntrinsicWidth();
                                        int imageHeight = resource.getIntrinsicHeight();

                                        // 固定宽度
                                        int fixedWidth = dpToPx(itemView.getContext(), 100);

                                        // 根据宽度计算高度，保持比例
                                        int calculatedHeight = (int) ((float) fixedWidth * imageHeight / imageWidth);

                                        // 设置 ImageView 的宽高
                                        ViewGroup.LayoutParams params = ivMyImage.getLayoutParams();
                                        params.width = fixedWidth;
                                        params.height = calculatedHeight;
                                        ivMyImage.setLayoutParams(params);
                                    }
                                    return false; // 返回 false 继续让 Glide 显示图片
                                }
                            })
                            .into(ivMyImage);

                            ivMyImage.setOnClickListener(v -> {
                                Intent intent = new Intent(itemView.getContext(), ImageViewActivity.class);
                                intent.putExtra("image_url", message.getFilePath());
                                itemView.getContext().startActivity(intent);
                            });
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

                ivOtherAvatar.setOnClickListener(v -> {
                    int contactId = ((MessageAdapter) ((RecyclerView) itemView.getParent()).getAdapter()).getContactId();
                    Intent intent = new Intent(itemView.getContext(), ContactInfoActivity.class);
                    intent.putExtra("contact_id", contactId);
                    itemView.getContext().startActivity(intent);
                });

                if ("text".equals(message.getMessageType())) {
                    tvOtherMessage.setVisibility(View.VISIBLE);
                    ivOtherImage.setVisibility(View.GONE);
                    tvOtherMessage.setText(message.getContent());
                } else if ("image".equals(message.getMessageType())) {
                    // TODO 图片比例
                    tvOtherMessage.setVisibility(View.GONE);
                    ivOtherImage.setVisibility(View.VISIBLE);
//                    otherMessageContent.setLayoutParams(new RelativeLayout.LayoutParams(
//                            RelativeLayout.LayoutParams.WRAP_CONTENT, 160));
                    Glide.with(itemView.getContext())
                            .load(message.getFilePath())
                            .skipMemoryCache(true) // 跳过内存缓存
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                            .placeholder(R.drawable.default_avatar) // 占位符
                            .error(R.drawable.default_avatar) // 加载失败时的图片
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    // 加载失败时处理
                                    return false; // 返回 false 继续让 Glide 显示 error 图片
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    // 图片加载成功后调整 ImageView 高度
                                    if (resource != null) {
                                        int imageWidth = resource.getIntrinsicWidth();
                                        int imageHeight = resource.getIntrinsicHeight();

                                        // 固定宽度
                                        int fixedWidth = dpToPx(itemView.getContext(), 100);

                                        // 根据宽度计算高度，保持比例
                                        int calculatedHeight = (int) ((float) fixedWidth * imageHeight / imageWidth);

                                        // 设置 ImageView 的宽高
                                        ViewGroup.LayoutParams params = ivMyImage.getLayoutParams();
                                        params.width = fixedWidth;
                                        params.height = calculatedHeight;
                                        ivOtherImage.setLayoutParams(params);
                                    }
                                    return false; // 返回 false 继续让 Glide 显示图片
                                }
                            })
                            .into(ivOtherImage);

                            ivOtherImage.setOnClickListener(v -> {
                                Intent intent = new Intent(itemView.getContext(), ImageViewActivity.class);
                                intent.putExtra("image_url", message.getFilePath());
                                itemView.getContext().startActivity(intent);
                            });
                }
            }
        }

        private int dpToPx(Context context, int dp) {
            return Math.round(dp * context.getResources().getDisplayMetrics().density);
        }

    }
}