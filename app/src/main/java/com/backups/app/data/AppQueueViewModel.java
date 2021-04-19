package com.backups.app.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.backups.app.utils.PackageNameUtils;
import java.util.ArrayList;
import java.util.List;

public class AppQueueViewModel extends ViewModel {
  private boolean mIsBackupInProgress = false;

  private String mBackupCountLabel = "";

  private long mBackupSize = 0L;

  private final BackupCreator mBackupCreator;

  private final List<APKFile> mSelectedApps = new ArrayList<>();

  private final MutableLiveData<BackupProgress> mProgressState =
      new MutableLiveData<>();

  private final MutableLiveData<List<APKFile>> mAppQueue =
      new MutableLiveData<>();

  public AppQueueViewModel(Context context) {
    mBackupCreator = new BackupCreator(context);
  }

  public boolean hasBackups() { return (!mSelectedApps.isEmpty()); }

  public boolean hasSufficientStorage() {
    return (mBackupSize == 0 ||
            mBackupCreator.hasSufficientStorage(mBackupSize));
  }

  public int getCurrentStorageVolumeIndex() {
    return mBackupCreator.getStorageVolumeIndex();
  }

  public boolean isBackupInProgress() { return mIsBackupInProgress; }

  public final String getBackupCountLabel() { return mBackupCountLabel; }

  public final List<APKFile> getSelectedApps() { return mSelectedApps; }

  public final LiveData<List<APKFile>> getAppQueueLiveData() {
    return mAppQueue;
  }

  public final LiveData<BackupProgress> getBackupProgressLiveData() {
    return mProgressState;
  }

  public void setBackupCountLabel(String backupCountLabel) {
    mBackupCountLabel = backupCountLabel;
  }

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

    mBackupSize += apkFile.getAppSize();
    mAppQueue.setValue(mSelectedApps);
  }

  private void resetProgressState() {
    BackupProgress result = mProgressState.getValue();

    if (result != null) {
      result.state = BackupProgress.ProgressState.NONE;
      result.backupName = "";
      result.progress = 0;
    }
  }

  public void startBackup() {
    mIsBackupInProgress = true;

    mBackupCreator.backup(mSelectedApps, mProgressState::postValue);

    resetProgressState();

    mBackupSize = 0L;

    mIsBackupInProgress = false;
  }
}
