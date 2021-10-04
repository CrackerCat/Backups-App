package com.backups.app.data.viewmodels.appqueue;

import android.content.Context;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.backups.app.data.events.DataEvent;
import com.backups.app.data.events.SelectionState;
import com.backups.app.data.pojos.ApkFile;
import com.backups.app.data.pojos.BackupProgress;
import java.util.Collections;
import java.util.List;

public final class AppQueueViewModel extends ViewModel {
  private boolean mIsBackupInProgress = false;

  private boolean mAutomaticallySelectedAll = false;

  private boolean mManuallySelectedAll = false;

  private SelectionState mSelectionState = SelectionState.NONE;

  private String mBackupCountLabel = "";

  private final AppQueueEntryHelper mAppQueueEntryHelper =
      new AppQueueEntryHelper();

  private final AppQueueHelper mAppQueueHelper = new AppQueueHelper();

  private final StorageVolumeHelper mStorageVolumeHelper;

  private final BackupHelper mBackupHelper;

  private final MutableLiveData<SelectionState> mSelectionStateLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<BackupProgress> mProgressState =
      new MutableLiveData<>();

  public AppQueueViewModel(Context context) {
    mStorageVolumeHelper = new StorageVolumeHelper(context);

    mBackupHelper =
        new BackupHelper(mStorageVolumeHelper.getStorageVolumePath());
  }

  public LiveData<BackupProgress> getBackupProgressLiveData() {
    return mProgressState;
  }

  public LiveData<DataEvent> getDataEventLiveData() {
    return mAppQueueHelper.getDataEventLiveData();
  }

  public LiveData<SelectionState> getSelectionStateLiveData() {
    return mSelectionStateLiveData;
  }

  public DataEvent getLastDataEvent() {
    return mAppQueueHelper.getLastDataEvent();
  }

  public SelectionState getCurrentSelectionState() { return mSelectionState; }

  public String getBackupCountLabel() { return mBackupCountLabel; }

  public boolean doesNotHaveBackups() {
    return mAppQueueHelper.getAppsInQueue().isEmpty();
  }

  public boolean isBackupInProgress() { return mIsBackupInProgress; }

  public boolean hasSelectedItems() {
    return !(mAppQueueHelper.getSelectedItems().isEmpty());
  }

  public boolean hasAutomaticallySelectedAll() {
    return mAutomaticallySelectedAll;
  }

  public boolean hasManuallySelectedAll() { return mManuallySelectedAll; }

  public int getSelectionSize() {
    return mAppQueueHelper.getSelectedItems().size();
  }

  public boolean hasSufficientStorage() {
    return mStorageVolumeHelper.hasSufficientStorage(
        mBackupHelper.getBackupSize());
  }

  public int getCurrentStorageVolumeIndex() {
    return mStorageVolumeHelper.getStorageVolumeIndex();
  }

  public String getOutputDirectoryPath() {
    return mStorageVolumeHelper.getStorageVolumePath();
  }

  public List<ApkFile> getAppsInQueue() {
    return Collections.unmodifiableList(mAppQueueHelper.getAppsInQueue());
  }

  public void isBackupInProgress(final boolean inProgress) {
    mIsBackupInProgress = inProgress;
  }

  public void hasAutomaticallySelectedAll(final boolean has) {
    mAutomaticallySelectedAll = has;
  }

  public void hasManuallySelectedAll(final boolean has) {
    mManuallySelectedAll = has;
  }

  public void setBackupCountLabel(String backupCount) {
    mBackupCountLabel = backupCount;
  }

  public boolean setStorageVolumeIndex(final int index) {
    final boolean outputDirectoryChanged =
        mStorageVolumeHelper.setStorageVolume(index);

    mBackupHelper.changeOutputDirectory(
        mStorageVolumeHelper.getStorageVolumePath());

    return outputDirectoryChanged;
  }

  public Pair<String[], String[]>
  getStorageEntryValues(final String primaryStorageFmt,
                        final String externalStorageFmt) {
    return mStorageVolumeHelper.makeStorageEntryValues(primaryStorageFmt,
                                                       externalStorageFmt);
  }

  public void setItemSelectionStateTo(final SelectionState state) {
    mSelectionStateLiveData.setValue((mSelectionState = state));

    if (state.equals(SelectionState.SELECTION_ENDED)) {
      mSelectionState = SelectionState.NONE;
    }
  }

  public void emptyQueue() {
    mAutomaticallySelectedAll = false;

    mManuallySelectedAll = false;

    mAppQueueHelper.emptyQueue();

    mAppQueueEntryHelper.clearRegisteredEntries();
  }

  public void updateSelectedApks(final ApkFile backup) {
    final boolean removedApk = mAppQueueHelper.addOrRemoveSelection(backup);

    if (removedApk) {
      if (backup.isDuplicate()) {
        mAppQueueEntryHelper.decrementCounterFor(backup.getPackageHash());
      }
    }
  }

  public void selectAll() {
    mAutomaticallySelectedAll = true;

    mAppQueueHelper.selectAll();
  }

  public void removeSelectedApks() {
    mAutomaticallySelectedAll = false;

    mManuallySelectedAll = false;

    mAppQueueHelper.removeSelected(
        (apk)
            -> mAppQueueEntryHelper.decrementCounterFor(apk.getPackageHash()));

    mSelectionStateLiveData.setValue(
        (mSelectionState = SelectionState.SELECTION_ENDED));

    mSelectionState = SelectionState.NONE;
  }

  public void clearSelection() {
    mAutomaticallySelectedAll = false;

    mAppQueueHelper.clearSelection();
  }

  public void addApp(ApkFile apkFile) {
    final String duplicateName =
        mAppQueueEntryHelper.computeDuplicateBackupName(
            apkFile.getPackageHash(), apkFile.getBackupName());

    if (duplicateName != null) {
      apkFile = new ApkFile(duplicateName, apkFile.getPackageName(),
                            apkFile.getPackagePath(), apkFile.getAppSize(),
                            apkFile.getIcon());
    }

    mAppQueueHelper.addAppToQueue(apkFile);

    mBackupHelper.incrementBackupSize(apkFile.getAppSize());
  }

  public void resetProgress() { mProgressState.setValue(new BackupProgress()); }

  public void startBackup() {
    mIsBackupInProgress = true;

    final List<ApkFile> appQueue = mAppQueueHelper.getAppsInQueue();

    if (mStorageVolumeHelper.canBackup(appQueue)) {

      mBackupHelper.backup(appQueue, (progress) -> {
        if (progress.finished()) {
          mBackupHelper.zeroBackupSize();

          mAppQueueEntryHelper.resetBackupCounter(progress.getBackupHash());
        }

        mProgressState.postValue(progress);
      });
    }
  }
}
