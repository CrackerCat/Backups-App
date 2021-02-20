package com.backups.app.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppQueueViewModel extends ViewModel {
  private final List<APKFile> mSelectedApps = new ArrayList<>();
  private static final HashMap<String, Integer> mTimesTable = new HashMap<>();


  private final MutableLiveData<List<APKFile>> mAppQueue =
      new MutableLiveData<>(mSelectedApps);

  public void addApp(APKFile apkFile) {
    String selectedAPKName = apkFile.getName();

    String repeatedBackupName = PackageNameUtils.computeRepeatedBackupName(selectedAPKName);
    if(repeatedBackupName != null) {
      APKFile repeat = new APKFile(repeatedBackupName, apkFile.getPackageName(), apkFile.getPackagePath(), apkFile.getAppSize(), apkFile.getIcon());
      mSelectedApps.add(repeat);
    } else {
      mSelectedApps.add(apkFile);
    }

    mAppQueue.setValue(mSelectedApps);
  }

  public int getSelectedAppCount() {return mSelectedApps.size();}

  public final LiveData<List<APKFile>> getAppQueue() {
    return mAppQueue;
  }
}
