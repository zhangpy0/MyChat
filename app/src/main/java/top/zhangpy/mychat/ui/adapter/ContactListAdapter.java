package top.zhangpy.mychat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.ContactListItem;
import top.zhangpy.mychat.ui.view.AddFriendActivity;
import top.zhangpy.mychat.ui.view.ContactInfoActivity;
import top.zhangpy.mychat.ui.view.GroupChatActivity;
import top.zhangpy.mychat.ui.view.PublicAccountActivity;
import top.zhangpy.mychat.ui.view.TagActivity;

// TODO glide 加载资源图片失败
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {

    private List<ContactListItem> contacts = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setContacts(List<ContactListItem> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactListAdapter.ContactViewHolder holder, int position) {
        ContactListItem contact = contacts.get(position);
        holder.contactName.setText(contact.getName());
        holder.itemViewLayout.setTag(contact.getId());

        holder.catalog.setVisibility(View.GONE); // 默认隐藏
        holder.divider.setVisibility(View.GONE); // 默认隐藏
        holder.contactCount.setVisibility(View.GONE); // 默认隐藏

        if ((contact.getAvatarPath() == null || contact.getAvatarPath().isEmpty()) && contact.getId() >= 0) {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.default_avatar)
                    .into(holder.avatar);
            return;
        } else if (contact.getId() < 0) {
            if (contact.getId() == -1) {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.newfriend)
                        .placeholder(R.drawable.newfriend)
                        .error(R.drawable.newfriend)
                        .into(holder.avatar);
            }
            if (contact.getId() == -2) {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.groupchat)
                        .placeholder(R.drawable.groupchat)
                        .error(R.drawable.groupchat)
                        .into(holder.avatar);
            }
            if (contact.getId() == -3) {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.sign)
                        .placeholder(R.drawable.sign)
                        .error(R.drawable.sign)
                        .into(holder.avatar);
            }
            if (contact.getId() == -4) {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.publicnum)
                        .placeholder(R.drawable.publicnum)
                        .error(R.drawable.publicnum)
                        .into(holder.avatar);
            }
        } else {

            Glide.with(holder.itemView.getContext())
                    .load(contact.getAvatarPath())
                    .skipMemoryCache(true) // 跳过内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                    .placeholder(R.drawable.default_avatar) // 占位符
                    .error(R.drawable.default_avatar) // 加载失败时的图片
                    .into(holder.avatar);
        }

        if (position == 0 || position == 1 || position == 2 || position == 3) {
            holder.catalog.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);
            holder.contactCount.setVisibility(View.GONE);
        } else {
            String catalog = contact.getNameSort().getFirstLetter();
            if (position == getPositionForSection(catalog)) {
                holder.catalog.setVisibility(View.VISIBLE);
                holder.catalog.setText(catalog);
            } else {
                holder.catalog.setVisibility(View.GONE);
            }
        }

        if (position == getItemCount() - 1) {
            holder.divider.setVisibility(View.VISIBLE);
            holder.contactCount.setVisibility(View.VISIBLE);
            holder.contactCount.setText(String.valueOf(getItemCount() - 4));
        } else {
            holder.divider.setVisibility(View.GONE);
            holder.contactCount.setVisibility(View.GONE);
        }

        // 设置点击事件
        holder.avatar.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent;
            switch (contact.getId()) {
                case -1: // 新的朋友
                    intent = new Intent(context, AddFriendActivity.class);
                    break;
                case -2: // 群聊
                    intent = new Intent(context, GroupChatActivity.class);
                    break;
                case -3: // 标签
                    intent = new Intent(context, TagActivity.class);
                    break;
                case -4: // 公众号
                    intent = new Intent(context, PublicAccountActivity.class);
                    break;
                default: // 其他联系人
                    intent = new Intent(context, ContactInfoActivity.class);
                    intent.putExtra("contact_id", contact.getId());
                    break;
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public ContactListItem getItem(int i) {
        return contacts.get(i);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        LinearLayout itemViewLayout;

        TextView catalog;
        TextView contactName;
        ImageView avatar;

        View divider;

        TextView contactCount;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemViewLayout = itemView.findViewById(R.id.contact_item);
            contactName = itemView.findViewById(R.id.contact_name);
            avatar = itemView.findViewById(R.id.contact_img);
            catalog = itemView.findViewById(R.id.catalog);
            divider = itemView.findViewById(R.id.divider);
            contactCount = itemView.findViewById(R.id.contact_count);
        }
    }

    public int getPositionForSection(String catalog) {
        for (int i = 0; i < getItemCount(); i++) {
            if (!Objects.equals(contacts.get(i).getName(), "新的朋友")
                    && !Objects.equals(contacts.get(i).getName(), "群聊")
                    && !Objects.equals(contacts.get(i).getName(), "标签")
                    && !Objects.equals(contacts.get(i).getName(), "公众号")) {
                String sortStr = contacts.get(i).getNameSort().getFirstLetter();
                if (catalog.equalsIgnoreCase(sortStr)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
