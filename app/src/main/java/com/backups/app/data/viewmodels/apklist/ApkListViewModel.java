package com.backups.app.data.viewmodels.apklist;

import android.content.pm.PackageManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.backups.app.data.pojos.ApkFile;
import java.util.List;
import java.util.concurrent.Executors;

public final class ApkListViewModel extends ViewModel {
  private final APKFileRepository mAPKFileRepository =
      new APKFileRepository(Executors.newSingleThreadExecutor());

  private final MutableLiveData<List<ApkFile>> mAppListMutableLiveData =
      new MutableLiveData<>();

  public boolean hasNotScannedForApps() {
    return (mAppListMutableLiveData.getValue() == null);
  }

  public final LiveData<List<ApkFile>> getApkListLiveData() {
    return mAppListMutableLiveData;
  }

  public void fetchInstalledApps(final PackageManager packageManager,
                                 boolean showSystemApps) {
    mAPKFileRepository.displaySystemApps(showSystemApps);

    mAPKFileRepository.fetchInstalledApps(packageManager,
                                          mAppListMutableLiveData::postValue);
  }

  public void clearApkData() { mAppListMutableLiveData.setValue(null); }
}
