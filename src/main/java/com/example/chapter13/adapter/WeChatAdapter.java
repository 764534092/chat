package com.example.chapter13.adapter;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.chapter13.fragment.FriendListFragment;
import com.example.chapter13.fragment.GroupListFragment;
import com.example.chapter13.fragment.MyInfoFragment;

public class WeChatAdapter extends FragmentPagerAdapter {

    public WeChatAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        Log.d("position1", String.valueOf(2));
    }


    @Override
    public Fragment getItem(int position) {
        Log.d("position", String.valueOf(position));
        if (position == 0) {
            return new FriendListFragment();
        } else if (position == 1) {
            return new GroupListFragment();
        } else if (position == 2) {
            return new MyInfoFragment();
        } else {
            return null;
        }
    }


    @Override
    public int getCount() {
        return 3;
    }
}
