package com.backups.app.data.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.repositories.AppQueueRepository;
import com.backups.app.data.repositories.BackupRepository;
import java.util.List;

public class AppQueueViewModel extends ViewModel {
  private boolean mIsBackupInProgress = false;

  private boolean mAutomaticallySelectedAll = false;

  private boolean mManuallySelectedAll = false;

  private ItemSelectionState mSelectionState = ItemSelectionState.NONE;

  private String mBackupCountLabel = "";

  private final MutableLiveData<ItemSelectionState> mSelectionStateLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<BackupProgress> mProgressState =
      new MutableLiveData<>();

  private final AppQueueRepository mAppQueueRepository =
      new AppQueueRepository();

  private final BackupRepository mBackupRepository;

  public AppQueueViewModel(Context context) {
    mBackupRepository = new BackupRepository(context);
  }

  public LiveData<BackupProgress> getBackupProgressLiveData() {
    return mProgressState;
  }

  public LiveData<DataEvent> getDataEventLiveData() {
    return mAppQueueRepository.getDataEventLiveData();
  }

  public LiveData<ItemSelectionState> getSelectionStateLiveData() {
    return mSelectionStateLiveData;
  }

  public DataEvent getLastDataEvent() {
    return mAppQueueRepository.getLastDataEvent();
  }

  public ItemSelectionState getCurrentSelectionState() {
    return mSelectionState;
  }

  public String getBackupCountLabel() { return mBackupCountLabel; }

  public boolean doesNotHaveBackups() {
    return mAppQueueRepository.getAppsInQueue().isEmpty();
  }

  public boolean isBackupInProgress() { return mIsBackupInProgress; }

  public boolean hasSelectedItems() {
    return !mAppQueueRepository.getSelectedItems().isEmpty();
  }

  public boolean hasAutomaticallySelectedAll() {
    return mAutomaticallySelectedAll;
  }

  public boolean hasManuallySelectedAll() { return mManuallySelectedAll; }

  public List<APKFile> getAppsInQueue() {
    return mAppQueueRepository.getAppsInQueue();
  }

  public int getSelectionSize() {
    return mAppQueueRepository.getSelectedItems().size();
  }

  public boolean hasSufficientStorage() {
    return mBackupRepository.hasSufficientStorage();
  }

  public int getCurrentStorageVolumeIndex() {
    return mBackupRepository.getStorageVolumeIndex();
  }

  public int getAvailableStorageVolumes() {
    return mBackupRepository.getAvailableStorageVolumeCount();
  }

  public String getStorageVolumePath() {
    return mBackupRepository.getStorageVolumePath();
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
    return mBackupRepository.setStorageVolume(index);
  }

  public void setItemSelectionStateTo(final ItemSelectionState state) {
    mSelectionStateLiveData.setValue((mSelectionState = state));

    if (state.equals(ItemSelectionState.SELECTION_ENDED)) {
      mSelectionState = ItemSelectionState.NONE;
    }
  }

  public void emptyQueue() {
    mAutomaticallySelectedAll = false;
    mManuallySelectedAll = false;

    mAppQueueRepository.emptyQueue();
  }

  public void addOrRemoveSelection(final APKFile backup) {
    mAppQueueRepository.addOrRemoveSelection(backup);
  }

  public void selectAll() {
    mAutomaticallySelectedAll = true;

    mAppQueueRepository.selectAll();
  }

  public void clearAndEmptySelection() {
    mAutomaticallySelectedAll = false;

    mManuallySelectedAll = false;

    mAppQueueRepository.clearAndEmptySelection();

    mSelectionStateLiveData.setValue(
        (mSelectionState = ItemSelectionState.SELECTION_ENDED));

    mSelectionState = ItemSelectionState.NONE;
  }

  public void clearSelection() {
    mAutomaticallySelectedAll = false;

    mAppQueueRepository.clearSelection();
  }

  public void addApp(APKFile apkFile) {
    mAppQueueRepository.addAppToQueue(apkFile);

    mBackupRepository.incrementBackupSize(apkFile.getAppSize());
  }

  public void resetProgress() { mProgressState.setValue(new BackupProgress()); }

  public void startBackup() {
    mIsBackupInProgress = true;

    mBackupRepository.backup(mAppQueueRepository.getAppsInQueue(),
                             mProgressState::postValue);

    mBackupRepository.zeroBackupSize();
  }
}
