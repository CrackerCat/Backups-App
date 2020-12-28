package com.backups.app.tabs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.backups.app.R;
import com.backups.app.fragments.AppListFragment;
import com.backups.app.fragments.AppQueueFragment;
import com.backups.app.fragments.SettingsFragment;

public class TabAdapter extends FragmentStateAdapter {

    private static final int[] TABS = {R.string.apps_tab_name, R.string.queue_tab_name, R.string.settings_tab_name};
    private final Fragment[] FRAGMENTS = {new AppListFragment(), new AppQueueFragment(), new SettingsFragment()};

    public TabAdapter(@NonNull FragmentActivity fragment) {
        super(fragment);
    }

    public CharSequence getTabName(Context context, int position) {
        if (position < TABS.length) {
            return context.getResources().getString(TABS[position]);
        }
        return "";
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FRAGMENTS[position];
    }

    @Override
    public int getItemCount() {
        return TABS.length;
    }
}
