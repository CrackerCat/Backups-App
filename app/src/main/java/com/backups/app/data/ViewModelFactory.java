package com.backups.app.data;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final PackageManager mPackageManager;
    private final IAPKFileRepository mApkFileRepository;

    public ViewModelFactory(final PackageManager packageManager, final IAPKFileRepository apkFileRepository) {
        mPackageManager = packageManager;
        mApkFileRepository = apkFileRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ApkListViewModel(mPackageManager, mApkFileRepository);
    }
}
