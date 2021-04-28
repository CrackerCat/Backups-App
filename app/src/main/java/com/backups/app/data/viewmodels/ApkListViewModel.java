package com.backups.app.data.viewmodels;

import android.content.pm.PackageManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.backups.app.data.APKFile;
import com.backups.app.data.repositories.APKFileRepository;
import java.util.List;
import java.util.concurrent.Executors;

public class ApkListViewModel extends ViewModel {
  private final APKFileRepository mAPKFileRepository =
      new APKFileRepository(Executors.newSingleThreadExecutor());

  private final MutableLiveData<List<APKFile>> mAppListMutableLiveData =
      new MutableLiveData<>();

  public void fetchInstalledApps(final PackageManager packageManager,
                                 boolean showSystemApps) {
    mAPKFileRepository.displaySystemApps(showSystemApps);
    mAPKFileRepository.fetchInstalledApps(packageManager,
                                          mAppListMutableLiveData::postValue);
  }

  public final LiveData<List<APKFile>> getApkListLiveData() {
    return mAppListMutableLiveData;
  }
}
