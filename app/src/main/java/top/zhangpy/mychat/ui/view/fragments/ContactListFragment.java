package top.zhangpy.mychat.ui.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.adapter.ContactListAdapter;
import top.zhangpy.mychat.ui.tools.SideBar;
import top.zhangpy.mychat.ui.viewmodel.ContactViewModel;
import top.zhangpy.mychat.util.Logger;

//  update异常 已解决
public class ContactListFragment extends Fragment {
    private ContactViewModel viewModel;
    private ContactListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contactlist, container, false);
        adapter = new ContactListAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.contact_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        SideBar sideBar = view.findViewById(R.id.side_bar);


        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        viewModel.getContactList().observe(getViewLifecycleOwner(), adapter::setContacts);

        sideBar.setOnSelectCallback(letter -> {
            if (adapter != null) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (adapter.getItem(i).getNameSort().getFirstLetter().equals(letter)) {
                        recyclerView.scrollToPosition(i);
                        break;
                    }
                }
            }
        });

        viewModel.updateContactListFromServer();

        Logger.initialize(requireContext());
        Logger.enableLogging(true);

        Logger.d("ContactListFragment", "updateContactListFromServer");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fragment 进入前台时更新联系人列表
//        viewModel.updateContactListFromServer();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            // Fragment 显示到前台时更新联系人列表
            Logger.d("ContactListFragment", "onHiddenChanged");
            viewModel.updateContactListFromServer();
            Logger.d("ContactListFragment", "updateContactListFromServer");
        }
    }
}
