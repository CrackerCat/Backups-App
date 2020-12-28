package com.backups.app;

import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.backups.app.filehandling.APKFileOperations;
import com.backups.app.tabs.TabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    TabAdapter mTabAdapter;
    TabLayout mTabLayout;
    ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mTabAdapter = new TabAdapter(this);

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
            if (requestCode == APKFileOperations.CREATE_DIRECTORY_CODE) {
                if (resultData != null) {
                    newDirectoryUri = resultData.getData();
                    DocumentFile pickedDirectory = DocumentFile.fromTreeUri(this, newDirectoryUri);
                    pickedDirectory.createDirectory(APKFileOperations.OUTPUT_DIRECTORY);
                }
            }
        }
    }

}