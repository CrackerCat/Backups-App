package com.backups.app.data;

import android.content.pm.PackageManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class ApkListViewModel extends ViewModel {
    private final APKFileRepository mAPKFileRepository;
    private final PackageManager mPackageManager;
    private boolean mHasReceivedApbList = false;
    private final MutableLiveData<List<APKFile>> mAppListMutableLiveData;


    public ApkListViewModel(PackageManager packageManager, APKFileRepository apkFileRepository) {
        mAPKFileRepository = apkFileRepository;
        mPackageManager = packageManager;
        mAppListMutableLiveData = new MutableLiveData<>();
        receiveInstalledApps();
    }

    private void receiveInstalledApps() {
        mAPKFileRepository.deliverInstalledApps(mPackageManager, mAppListMutableLiveData::postValue);
    }

    public boolean hasNotReceivedApkList() {
        return !mHasReceivedApbList;
    }

    public void receivedApkList(boolean choice) {
        mHasReceivedApbList = choice;
    }

    public LiveData<List<APKFile>> getApkData() {
        return mAppListMutableLiveData;
    }
}
