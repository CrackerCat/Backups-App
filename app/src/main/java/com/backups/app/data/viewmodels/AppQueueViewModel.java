package com.backups.app.data.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.backups.app.data.APKFile;
import com.backups.app.data.BackupProgress;
import com.backups.app.data.repositories.BackupRepository;
import com.backups.app.utils.PackageNameUtils;
import java.util.ArrayList;
import java.util.List;

public class AppQueueViewModel extends ViewModel {
  public enum DataEvent { NONE, ITEM_ADDED, ITEM_REMOVED }

  private boolean mIsBackupInProgress = false;

  private String mBackupCountLabel = "";

  private long mBackupSize = 0L;

  private final BackupRepository mBackupRepository;

  private final List<APKFile> mSelectedApps = new ArrayList<>();

  private final MutableLiveData<BackupProgress> mProgressState =
      new MutableLiveData<>();

  private final MutableLiveData<List<APKFile>> mAppQueue =
      new MutableLiveData<>();

  private DataEvent mLastDataEvent = DataEvent.NONE;

  public AppQueueViewModel(Context context) {
    mBackupRepository = new BackupRepository(context);
  }

  public boolean doesNotHaveBackups() { return mSelectedApps.isEmpty(); }

  public boolean hasSufficientStorage() {
    return (mBackupSize == 0 ||
            mBackupRepository.hasSufficientStorage(mBackupSize));
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

  public int getCurrentStorageVolumeIndex() {
    return mBackupRepository.getStorageVolumeIndex();
  }

  public final int getAvailableStorageVolumes() {
    return mBackupRepository.getAvailableStorageVolumeCount();
  }

  public final String getStorageVolumePath() {
    return mBackupRepository.getStorageVolumePath();
  }

  public DataEvent getLastDataEvent() {
    DataEvent lastDataEvent = mLastDataEvent;

    mLastDataEvent = DataEvent.NONE;

    return lastDataEvent;
  }

  public void setBackupCountLabel(String backupCountLabel) {
    mBackupCountLabel = backupCountLabel;
  }

  public boolean setStorageVolumeIndex(final int index) {
    return mBackupRepository.setStorageVolume(index);
  }

  public void updateSelection(final DataEvent currentEvent) {
    mLastDataEvent = currentEvent;
    mAppQueue.setValue(mSelectedApps);
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

    mBackupRepository.backup(mSelectedApps, mProgressState::postValue);

    resetProgressState();

    mBackupSize = 0L;

    mIsBackupInProgress = false;
  }
}
