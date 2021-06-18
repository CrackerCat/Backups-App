package com.backups.app.data.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.repositories.BackupRepository;
import java.util.List;

public final class BackupsViewModel extends ViewModel {
  private boolean mIsBackupInProgress = false;

  private final MutableLiveData<BackupProgress> mProgressState =
      new MutableLiveData<>();

  private final MutableLiveData<BackupRepository.OutputStorage>
      mOutputDirectoryStatus = new MutableLiveData<>();

  private final BackupRepository mBackupRepository;

  public BackupsViewModel(final Context context) {
    mBackupRepository = new BackupRepository(context);
  }

  public LiveData<BackupProgress> getBackupProgressLiveData() {
    return mProgressState;
  }

  public boolean isBackupInProgress() { return mIsBackupInProgress; }

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

  public BackupRepository.OutputStorage isOutputDirectoryMounted() {
    return mBackupRepository.isOutputDirectoryMounted(
        mBackupRepository.getStorageVolumeIndex());
  }

  public void isBackupInProgress(final boolean inProgress) {
    mIsBackupInProgress = inProgress;
  }

  public boolean setStorageVolumeIndex(final int index) {
    return mBackupRepository.setStorageVolume(index);
  }

  public void incrementBackupSize(final long backupSize) {
    if (backupSize != 0L) {
      mBackupRepository.incrementBackupSize(backupSize);
    }
  }

  public void endBackup() {
    BackupProgress finalProgress = new BackupProgress();

    finalProgress.setState(BackupProgress.ProgressState.ENDED);

    mProgressState.setValue(finalProgress);
  }

  public void resetProgress() { mProgressState.setValue(new BackupProgress()); }

  public void startBackup(final List<APKFile> backups) {
    mIsBackupInProgress = true;

    mBackupRepository.backup(backups, mProgressState::postValue);

    mBackupRepository.zeroBackupSize();
  }
}
