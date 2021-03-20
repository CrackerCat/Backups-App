package com.backups.app.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class AppQueueViewModel extends ViewModel {
  private int mPreviousSize = -1;
  private String mBackupCountLabel = "";

  private final List<APKFile> mSelectedApps = new ArrayList<>();

  private final MutableLiveData<List<APKFile>> mAppQueue =
      new MutableLiveData<>();

  public void addApp(APKFile apkFile) {
    String selectedAPKName = apkFile.getName();

    String repeatedBackupName =
        PackageNameUtils.computeRepeatedBackupName(selectedAPKName);
    if (repeatedBackupName != null) {
      APKFile repeat = new APKFile(repeatedBackupName, apkFile.getPackageName(),
                                   apkFile.getPackagePath(),
                                   apkFile.getAppSize(), apkFile.getIcon());
      mSelectedApps.add(repeat);
    } else {
      mSelectedApps.add(apkFile);
    }

    mAppQueue.setValue(mSelectedApps);
  }

  public void setBackupCountLabel(String backupCountLabel) {
    mBackupCountLabel = backupCountLabel;
  }

  public final String getBackupCountLabel() { return mBackupCountLabel; }

  public boolean hasBackups() { return (!mSelectedApps.isEmpty()); }

  public void updateSelection() {
    int updatedSize = mSelectedApps.size();
    if (mPreviousSize < updatedSize) {
      mAppQueue.postValue(mSelectedApps);
      mPreviousSize = updatedSize;
    }
  }

  public final List<APKFile> getSelectedApps() { return mSelectedApps; }

  public final LiveData<List<APKFile>> getAppQueueLiveData() {
    return mAppQueue;
  }
}
