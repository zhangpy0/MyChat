package top.zhangpy.mychat.ui.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import top.zhangpy.mychat.R
import top.zhangpy.mychat.ui.model.GroupMessageListItem
import top.zhangpy.mychat.ui.view.ContactInfoActivity
import top.zhangpy.mychat.ui.view.FileViewActivity
import top.zhangpy.mychat.ui.view.ImageViewActivity
import top.zhangpy.mychat.util.StorageHelper
import java.lang.ref.WeakReference

class GroupMessageAdapter(
    private val context: Context,
    private val messages: List<GroupMessageListItem>,
    private val myAvatarPath: String,
    private val avatarMap: Map<Int, String>, // 群组成员头像映射
    private val currentUserId: Int,          // 当前用户ID
    private val groupId: Int                // 当前群组ID
) : RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder>() {

    inner class GroupMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 他人消息视图
        private val otherMessageContainer: LinearLayout = itemView.findViewById(R.id.other_message_container)
        private val ivOtherAvatar: ImageView = itemView.findViewById(R.id.iv_other_avatar)
        private val tvSenderName: TextView = itemView.findViewById(R.id.tv_sender_name)
        private val tvOtherMessage: TextView = itemView.findViewById(R.id.tv_other_message)
        private val ivOtherImage: ImageView = itemView.findViewById(R.id.iv_other_image)
        private val llOtherFile: LinearLayout = itemView.findViewById(R.id.ll_other_file)
        private val tvOtherFileName: TextView = itemView.findViewById(R.id.tv_other_file_name)
        private val tvOtherFileSize: TextView = itemView.findViewById(R.id.tv_other_file_size)

        // 自己消息视图
        private val myMessageContainer: RelativeLayout = itemView.findViewById(R.id.my_message_container)
        private val ivMyAvatar: ImageView = itemView.findViewById(R.id.iv_my_avatar)
        private val tvMyMessage: TextView = itemView.findViewById(R.id.tv_my_message)
        private val ivMyImage: ImageView = itemView.findViewById(R.id.iv_my_image)
        private val llMyFile: LinearLayout = itemView.findViewById(R.id.ll_my_file)
        private val tvMyFileName: TextView = itemView.findViewById(R.id.tv_my_file_name)
        private val tvMyFileSize: TextView = itemView.findViewById(R.id.tv_my_file_size)

        fun bind(message: GroupMessageListItem) {
            clearAllViews()
            if (message.senderId == currentUserId) {
                showMyMessage(message)
            } else {
                showOtherMessage(message)
            }
        }

        private fun showMyMessage(message: GroupMessageListItem) {
            myMessageContainer.visibility = View.VISIBLE
            loadAvatar(ivMyAvatar, myAvatarPath)

            when (message.messageType) {
                "text" -> message.content?.let {
                    showTextMessage(tvMyMessage, llMyFile, ivMyImage,
                        it
                    )
                }
                "image" -> message.filePath?.let {
                    showImageMessage(ivMyImage, tvMyMessage, llMyFile,
                        it
                    )
                }
                "file" -> showFileMessage(llMyFile, tvMyFileName, tvMyFileSize, tvMyMessage, ivMyImage, message)
            }
        }

        private fun showOtherMessage(message: GroupMessageListItem) {
            otherMessageContainer.visibility = View.VISIBLE
            tvSenderName.text = message.senderName
            loadAvatar(ivOtherAvatar, avatarMap[message.senderId] ?: "")

            when (message.messageType) {
                "text" -> message.content?.let {
                    showTextMessage(tvOtherMessage, llOtherFile, ivOtherImage,
                        it
                    )
                }
                "image" -> message.filePath?.let {
                    showImageMessage(ivOtherImage, tvOtherMessage, llOtherFile,
                        it
                    )
                }
                "file" -> showFileMessage(llOtherFile, tvOtherFileName, tvOtherFileSize, tvOtherMessage, ivOtherImage, message)
            }

            setupAvatarClick(ivOtherAvatar, message.senderId)
        }

        private fun showTextMessage(
            textView: TextView,
            fileContainer: LinearLayout,
            imageView: ImageView,
            content: String
        ) {
            textView.visibility = View.VISIBLE
            textView.text = content
            fileContainer.visibility = View.GONE
            imageView.visibility = View.GONE
        }

        private fun showImageMessage(
            imageView: ImageView,
            textView: TextView,
            fileContainer: LinearLayout,
            imagePath: String
        ) {
            imageView.visibility = View.VISIBLE
            textView.visibility = View.GONE
            fileContainer.visibility = View.GONE
            loadImageWithRatio(imageView, imagePath)
            setupImageClick(imageView, imagePath)
        }

        private fun showFileMessage(
            fileContainer: LinearLayout,
            fileName: TextView,
            fileSize: TextView,
            textView: TextView,
            imageView: ImageView,
            message: GroupMessageListItem
        ) {
            fileContainer.visibility = View.VISIBLE
            fileName.text = message.fileName
            fileSize.text = message.fileSize?.let { StorageHelper.formatFileSize(it) }
            textView.visibility = View.GONE
            imageView.visibility = View.GONE
            setupFileClick(fileContainer, message)
        }

        private fun loadAvatar(imageView: ImageView, avatarPath: String) {
            Glide.with(imageView.context)
                .load(avatarPath.ifEmpty { R.drawable.default_avatar })
                .apply(RequestOptions()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar))
                .into(imageView)
        }

        private fun loadImageWithRatio(imageView: ImageView, imagePath: String) {
            Glide.with(imageView.context)
                .load(imagePath)
                .addListener(ImageSizeAdjustListener(imageView))
                .into(imageView)
        }

        private fun setupImageClick(imageView: ImageView, imagePath: String) {
            imageView.setOnClickListener {
                val intent = Intent(context, ImageViewActivity::class.java).apply {
                    putExtra("image_url", imagePath)
                }
                context.startActivity(intent)
            }
        }

        private fun setupFileClick(fileContainer: ViewGroup, message: GroupMessageListItem) {
            fileContainer.setOnClickListener {
                val intent = Intent(context, FileViewActivity::class.java).apply {
                    putExtra("id", message.id)
                    putExtra("file_path", message.filePath)
                    putExtra("file_name", message.fileName)
                    putExtra("file_size", message.fileSize)
                    putExtra("is_me", message.senderId == currentUserId)
                    putExtra("content", message.content)
                    putExtra("contact_id", groupId)
                    putExtra("contact_type", "group")
                }
                context.startActivity(intent)
            }
        }

        private fun setupAvatarClick(avatarView: ImageView, senderId: Int) {
            avatarView.setOnClickListener {
                val intent = Intent(context, ContactInfoActivity::class.java).apply {
                    putExtra("contact_id", senderId)
                    putExtra("group_id", groupId)
                }
                context.startActivity(intent)
            }
        }

        private fun clearAllViews() {
            // 重置自己消息视图
            myMessageContainer.visibility = View.GONE
            tvMyMessage.text = ""
            ivMyImage.setImageDrawable(null)
            llMyFile.visibility = View.GONE

            // 重置他人消息视图
            otherMessageContainer.visibility = View.GONE
            tvSenderName.text = ""
            tvOtherMessage.text = ""
            ivOtherImage.setImageDrawable(null)
            llOtherFile.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.group_message_item, parent, false)
        return GroupMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    private class ImageSizeAdjustListener(imageView: ImageView) : RequestListener<Drawable> {
        private val imageViewRef = WeakReference(imageView)
        private val minSizeDp = 100

        // 修正后的方法签名（注意移除了target的可空标识）
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>,
            isFirstResource: Boolean
        ): Boolean {
            // 可以在这里添加错误处理逻辑
            return false
        }

        // 修正后的方法签名（注意target参数类型）
        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            val imageView = imageViewRef.get() ?: return false
            adjustWithMinSize(imageView, resource)
            return false
        }

        private fun adjustWithMinSize(imageView: ImageView, resource: Drawable) {
            val context = imageView.context
            val imageWidth = resource.intrinsicWidth
            val imageHeight = resource.intrinsicHeight

            if (imageWidth <= 0 || imageHeight <= 0) return

            val minSizePx = dpToPx(context, minSizeDp)
            val isWide = imageWidth > imageHeight
            val aspectRatio = imageWidth.toFloat() / imageHeight

            val (calculatedWidth, calculatedHeight) = when {
                isWide -> Pair((minSizePx * aspectRatio).toInt(), minSizePx)
                else -> Pair(minSizePx, (minSizePx / aspectRatio).toInt())
            }

            imageView.post {
                val params = imageView.layoutParams.apply {
                    width = maxOf(calculatedWidth, minSizePx)
                    height = maxOf(calculatedHeight, minSizePx)
                }
                imageView.layoutParams = params
            }
        }
        private fun dpToPx(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
    }
}