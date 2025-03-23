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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.lang.ref.WeakReference;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.MessageListItem;
import top.zhangpy.mychat.ui.view.ContactInfoActivity;
import top.zhangpy.mychat.ui.view.FileViewActivity;
import top.zhangpy.mychat.ui.view.ImageViewActivity;
import top.zhangpy.mychat.util.Logger;
import top.zhangpy.mychat.util.StorageHelper;

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

    private record MessageViews(ViewGroup container, ImageView avatar, TextView textView,
                                ImageView imageView, ViewGroup fileContainer, TextView fileName,
                                TextView fileSize) {
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        // 对方消息视图
        private final LinearLayout otherMessageContainer;
        private final LinearLayout otherMessageContent;
        private final ImageView ivOtherAvatar;
        private final TextView tvOtherMessage;
        private final ImageView ivOtherImage;

        private final LinearLayout llOtherFile;

        private final TextView tvOtherFileName;

        private final TextView tvOtherFileSize;


        // 自己消息视图
        private final RelativeLayout myMessageContainer;
        private final LinearLayout myMessageContent;
        private final ImageView ivMyAvatar;
        private final TextView tvMyMessage;
        private final ImageView ivMyImage;

        private final LinearLayout llMyFile;

        private final TextView tvMyFileName;

        private final TextView tvMyFileSize;

        private String myAvatarPath;

        private String otherAvatarPath;

        private final MessageViews myViews;
        private final MessageViews otherViews;

        public MessageViewHolder(@NonNull View itemView, String myAvatarPath, String otherAvatarPath) {
            super(itemView);

            myViews = new MessageViews(
                    itemView.findViewById(R.id.my_message_container),
                    itemView.findViewById(R.id.iv_my_avatar),
                    itemView.findViewById(R.id.tv_my_message),
                    itemView.findViewById(R.id.iv_my_image),
                    itemView.findViewById(R.id.ll_my_file),
                    itemView.findViewById(R.id.tv_my_file_name),
                    itemView.findViewById(R.id.tv_my_file_size)
            );

            otherViews = new MessageViews(
                    itemView.findViewById(R.id.other_message_container),
                    itemView.findViewById(R.id.iv_other_avatar),
                    itemView.findViewById(R.id.tv_other_message),
                    itemView.findViewById(R.id.iv_other_image),
                    itemView.findViewById(R.id.ll_other_file),
                    itemView.findViewById(R.id.tv_other_file_name),
                    itemView.findViewById(R.id.tv_other_file_size)
            );

            // 初始化对方消息视图
            otherMessageContainer = itemView.findViewById(R.id.other_message_container);
            otherMessageContent = itemView.findViewById(R.id.other_message_content);
            ivOtherAvatar = itemView.findViewById(R.id.iv_other_avatar);
            tvOtherMessage = itemView.findViewById(R.id.tv_other_message);
            ivOtherImage = itemView.findViewById(R.id.iv_other_image);
            llOtherFile = itemView.findViewById(R.id.ll_other_file);
            tvOtherFileName = itemView.findViewById(R.id.tv_other_file_name);
            tvOtherFileSize = itemView.findViewById(R.id.tv_other_file_size);



            // 初始化自己消息视图
            myMessageContainer = itemView.findViewById(R.id.my_message_container);
            myMessageContent = itemView.findViewById(R.id.my_message_content);
            ivMyAvatar = itemView.findViewById(R.id.iv_my_avatar);
            tvMyMessage = itemView.findViewById(R.id.tv_my_message);
            ivMyImage = itemView.findViewById(R.id.iv_my_image);
            llMyFile = itemView.findViewById(R.id.ll_my_file);
            tvMyFileName = itemView.findViewById(R.id.tv_my_file_name);
            tvMyFileSize = itemView.findViewById(R.id.tv_my_file_size);

            this.myAvatarPath = myAvatarPath;
            this.otherAvatarPath = otherAvatarPath;
        }

        public void bind(MessageListItem message) {
            clearAllViews();
            MessageViews targetViews = message.isMe() ? myViews : otherViews;
            showMessage(targetViews, message, message.isMe());
        }

        private void showMessage(MessageViews views, MessageListItem message, boolean isMe) {
            // 显示目标容器
            setContainerVisible(views.container, true);

            // 加载头像
            loadAvatar(views.avatar, isMe ? myAvatarPath : otherAvatarPath);

            // 处理消息内容
            switch (message.getMessageType()) {
                case "text":
                    showTextMessage(views, message.getContent());
                    break;
                case "image":
                    showImageMessage(views, message.getFilePath());
                    setupImageClick(views.imageView, message.getFilePath());
                    break;
                case "file":
                    showFileMessage(views, message);
                    setupFileClick(views.fileContainer, message, isMe);
                    break;
            }

            // 特殊处理对方头像点击
            if (!isMe) {
                setupAvatarClick(views.avatar);
            }
        }

        private void showTextMessage(MessageViews views, String content) {
            setViewVisibility(views.textView, View.VISIBLE);
            setViewVisibility(views.imageView, View.GONE);
            setViewVisibility(views.fileContainer, View.GONE);
            views.textView.setText(content);
        }

        private void showImageMessage(MessageViews views, String imagePath) {
            setViewVisibility(views.textView, View.GONE);
            setViewVisibility(views.imageView, View.VISIBLE);
            setViewVisibility(views.fileContainer, View.GONE);

            loadImageWithRatio(views.imageView, imagePath);
        }

        private void showFileMessage(MessageViews views, MessageListItem message) {
            setViewVisibility(views.textView, View.GONE);
            setViewVisibility(views.imageView, View.GONE);
            setViewVisibility(views.fileContainer, View.VISIBLE);

            views.fileName.setText(message.getFileName());
            views.fileSize.setText(StorageHelper.formatFileSize(message.getFileSize()));
        }

        private void setContainerVisible(ViewGroup container, boolean visible) {
            container.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        private void setViewVisibility(View view, int visibility) {
            view.setVisibility(visibility);
        }

        private void loadAvatar(ImageView avatarView, String avatarPath) {
            Glide.with(avatarView.getContext())
                    .load(avatarPath)
                    .apply(new RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar))
                    .into(avatarView);
        }

        private void loadImageWithRatio(ImageView imageView, String imagePath) {
            Glide.with(imageView.getContext())
                    .load(imagePath)
                    .addListener(new ImageSizeAdjustListener(imageView))
                    .into(imageView);
        }

        private static class ImageSizeAdjustListener implements RequestListener<Drawable> {
            private final WeakReference<ImageView> imageViewRef;
            private final int minSizeDp; // 添加可配置参数

            ImageSizeAdjustListener(ImageView imageView) {
                this(imageView, 100); // 默认 100dp
            }

            ImageSizeAdjustListener(ImageView imageView, int maxWidthDp) {
                this.imageViewRef = new WeakReference<>(imageView);
                this.minSizeDp = maxWidthDp;
            }

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) {
                Logger.e("ImageSizeAdjustListener", "onLoadFailed: ", e);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model,
                                           Target<Drawable> target, DataSource dataSource,
                                           boolean isFirstResource) {
                ImageView targetIv = imageViewRef.get();
                if (targetIv != null && targetIv.getContext() != null && resource != null) {
                    adjustWithMinSize(targetIv, resource);
                }
                return false; // 允许 Glide 继续处理
            }

            private void adjustWithMinSize(ImageView imageView, Drawable resource) {
                Context context = imageView.getContext();
                int imageWidth = resource.getIntrinsicWidth();
                int imageHeight = resource.getIntrinsicHeight();

                // 处理无效尺寸
                if (imageWidth <= 0 || imageHeight <= 0) return;

                // 转换为像素值
                final int minSizePx = dpToPx(context, minSizeDp);

                // 计算比例关系
                final boolean isWide = imageWidth > imageHeight;
                final float aspectRatio = (float) imageWidth / imageHeight;

                // 第一阶段：基于比例设置基准维度
                int baseSize = minSizePx;
                int calculatedWidth, calculatedHeight;

                if (isWide) {  // 宽图：固定高度为minSize
                    calculatedHeight = baseSize;
                    calculatedWidth = (int) (baseSize * aspectRatio);
                } else {       // 高图或方图：固定宽度为minSize
                    calculatedWidth = baseSize;
                    calculatedHeight = (int) (baseSize / aspectRatio);
                }

                // 第二阶段：强制双维度最小值
                calculatedWidth = Math.max(calculatedWidth, minSizePx);
                calculatedHeight = Math.max(calculatedHeight, minSizePx);

                // 最终线程安全更新
                int finalCalculatedWidth = calculatedWidth;
                int finalCalculatedHeight = calculatedHeight;
                imageView.post(() -> {
                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                    params.width = finalCalculatedWidth;
                    params.height = finalCalculatedHeight;
                    imageView.setLayoutParams(params);
                });
            }
        }

        private void setupImageClick(ImageView imageView, String imagePath) {
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ImageViewActivity.class);
                intent.putExtra("image_url", imagePath);
                v.getContext().startActivity(intent);
            });
        }

        private void setupFileClick(ViewGroup fileContainer, MessageListItem message, boolean isMe) {
            fileContainer.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FileViewActivity.class);
                intent.putExtra("id", message.getId());
                intent.putExtra("file_path", message.getFilePath());
                intent.putExtra("file_name", message.getFileName());
                intent.putExtra("file_size", message.getFileSize());
                intent.putExtra("is_me", false);
                intent.putExtra("content", message.getContent());
                intent.putExtra("contact_id", ((MessageAdapter) ((RecyclerView) itemView.getParent()).getAdapter()).getContactId());
                intent.putExtra("contact_type", "user");
                v.getContext().startActivity(intent);
            });
        }

        private void setupAvatarClick(ImageView avatarView) {
            avatarView.setOnClickListener(v -> {
                int contactId = ((MessageAdapter) ((RecyclerView) v.getParent()).getAdapter()).getContactId();
                Intent intent = new Intent(v.getContext(), ContactInfoActivity.class);
                intent.putExtra("contact_id", contactId);
                v.getContext().startActivity(intent);
            });
        }

        private void clearAllViews() {
            // 重置我的消息容器
            resetMessageViews(myViews);
            myViews.container.setVisibility(View.GONE);

            // 重置对方消息容器
            resetMessageViews(otherViews);
            otherViews.container.setVisibility(View.GONE);
        }

        private void resetMessageViews(MessageViews views) {
            // 文本消息
            views.textView.setVisibility(View.GONE);
            views.textView.setText("");

            // 图片消息
            views.imageView.setVisibility(View.GONE);
            views.imageView.setImageDrawable(null); // 清除残留图片
            Glide.with(views.imageView).clear(views.imageView); // 取消可能存在的 Glide 请求

            // 文件消息
            views.fileContainer.setVisibility(View.GONE);
            views.fileName.setText("");
            views.fileSize.setText("");

            // 头像
            views.avatar.setImageDrawable(null);
            Glide.with(views.avatar).clear(views.avatar);
        }

        private static int dpToPx(Context context, int dp) {
            return Math.round(dp * context.getResources().getDisplayMetrics().density);
        }

    }
}