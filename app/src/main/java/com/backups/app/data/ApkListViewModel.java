package com.backups.app.data;

import android.content.pm.PackageManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ApkListViewModel extends ViewModel {
  private final APKFileRepository mAPKFileRepository =
      new APKFileRepository(Executors.newSingleThreadExecutor());
  private final MutableLiveData<List<APKFile>> mAppListMutableLiveData =
      new MutableLiveData<>();

  public void fetchInstalledApps(final PackageManager packageManager,
                                 boolean showSystemApps) {
    if (mAppListMutableLiveData.getValue() == null) {
      mAPKFileRepository.displaySystemApps(showSystemApps);
      mAPKFileRepository.fetchInstalledApps(packageManager, result -> {
        if (result != null) {
          mAppListMutableLiveData.postValue(result);
        } else {
          mAppListMutableLiveData.postValue(new ArrayList<>());
        }
      });
    }
  }

  public final LiveData<List<APKFile>> getApkListLiveData() {
    return mAppListMutableLiveData;
  }
}
