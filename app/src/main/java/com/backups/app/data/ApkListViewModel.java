package com.backups.app.data;

import android.content.pm.PackageManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ApkListViewModel extends ViewModel {
  private final IAPKFileRepository mAPKFileRepository =
      new APKFileRepository(Executors.newSingleThreadExecutor());
  private final MutableLiveData<List<APKFile>> mAppListMutableLiveData =
      new MutableLiveData<>();
  private boolean mHasSuccessfullyFetchedData = false;

  public void fetchInstalledApps(final PackageManager packageManager) {
    if (mAppListMutableLiveData.getValue() == null) {
      mAPKFileRepository.fetchInstalledApps(packageManager, result -> {
        if (result != null) {
          mAppListMutableLiveData.postValue(result);
        } else {
          mAppListMutableLiveData.postValue(new ArrayList<>());
        }
      });
    }
  }

  public void hasFetchedData(boolean flag) {
    mHasSuccessfullyFetchedData = flag;
  }

  public boolean hasSuccessfullyFetchedData() {
    return mHasSuccessfullyFetchedData;
  }

  public final LiveData<List<APKFile>> getApkListLiveData() {
    return mAppListMutableLiveData;
  }
}
