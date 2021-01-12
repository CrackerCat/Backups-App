package com.backups.app;

import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.backups.app.data.APKFileRepository;
import com.backups.app.permissionshandler.PermissionsHandler;
import com.backups.app.ui.adapters.TabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private TabAdapter mTabAdapter;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabAdapter = new TabAdapter(this);

        mTabAdapter = new TabAdapter(this);

        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mTabAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(mTabAdapter.getTabName(this, position))
        ).attach();
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
}