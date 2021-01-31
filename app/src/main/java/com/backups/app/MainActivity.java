package com.backups.app;

import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.backups.app.data.APKFileRepository;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.permissionshandler.PermissionsHandler;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.OnFragmentInteractionListener;
import com.backups.app.ui.fragments.SettingsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {
    private AppQueueViewModel mAppQueueViewModel;

    private TextView mSelectedApps;

    private TabAdapter mTabAdapter;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppQueueViewModel = new ViewModelProvider(this).get(AppQueueViewModel.class);

        initializeViews();

        initializeTabLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        Uri newDirectoryUri;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PermissionsHandler.CREATE_DIRECTORY_CODE) {
                if (resultData != null) {
                    newDirectoryUri = resultData.getData();
                    DocumentFile pickedDirectory = DocumentFile.fromTreeUri(this, newDirectoryUri);
                    if (pickedDirectory != null) {
                        pickedDirectory.createDirectory(APKFileRepository.getOutputDirectory());
                    }
                }
            }
        }
    }

    private void addTabs(TabAdapter tabAdapter) {
        tabAdapter.addTab(getString(R.string.apps_tab_name), new AppListFragment());
        tabAdapter.addTab(getString(R.string.queue_tab_name), new AppQueueFragment());
        tabAdapter.addTab(getString(R.string.settings_tab_name), new SettingsFragment());
    }

    private void initializeViews() {
        mSelectedApps = (TextView) findViewById(R.id.apps_count_label);

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.pager);
    }

    private void initializeTabLayout() {
        mTabAdapter = new TabAdapter(this);

        addTabs(mTabAdapter);

        mViewPager.setAdapter(mTabAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(mTabAdapter.getTabName(position))
        ).attach();
    }

    @Override
    public void onCall() {
        mSelectedApps.setText(mAppQueueViewModel.getTotal());
    }
}