package com.chitchat.database;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.chitchat.activity.AddFriendsFragment;
import com.chitchat.activity.ChatListFragment;
import com.chitchat.activity.MessageRequestFragment;

public class pageAdapter extends FragmentStateAdapter {
    private String [] titles = new String[]{"Chats","Requests","Add Friends"};
    String userId;
    public pageAdapter(@NonNull FragmentActivity fragmentActivity,String userId) {
        super(fragmentActivity);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position)
        {
            case 0:
                return new ChatListFragment(userId);
            case 1:
                return new MessageRequestFragment(userId);
            case 2:
                return new AddFriendsFragment(userId);
        }
        return new ChatListFragment(userId);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
