package com.backups.app.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.LinkedList;

public class TabAdapter extends FragmentStateAdapter {
    private final LinkedList<String> mTabNames;
    private final LinkedList<Fragment> mTabs;

    public TabAdapter(@NonNull FragmentActivity fragment) {
        super(fragment);
        mTabNames = new LinkedList<>();
        mTabs = new LinkedList<>();
    }

    public void addTab(final String name, final Fragment fragment) {
        mTabNames.add(name);
        mTabs.add(fragment);
    }

    public CharSequence getTabName(int position) {
        if (position < mTabs.size()) {
            return mTabNames.get(position);
        }
        return  "";
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mTabs.get(position);
    }

    @Override
    public int getItemCount() {
        return mTabs.size();
    }
}
