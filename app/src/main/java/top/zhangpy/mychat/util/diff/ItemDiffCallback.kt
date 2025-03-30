package top.zhangpy.mychat.util.diff

import androidx.recyclerview.widget.DiffUtil
import top.zhangpy.mychat.ui.model.GroupMessageListItem
import top.zhangpy.mychat.ui.model.MessageListItem

class ItemDiffCallback  {
    inner class MessageDiffCallback(private val oldList: List<MessageListItem>, private val newList: List<MessageListItem>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldList[oldPos].id == newList[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldList[oldPos] == newList[newPos]
    }

    inner class GroupMessageDiffCallback(private val oldList: List<GroupMessageListItem>, private val newList: List<GroupMessageListItem>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldList[oldPos].id == newList[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldList[oldPos] == newList[newPos]
    }
}