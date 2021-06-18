package com.backups.app.data.repositories;

import android.content.Context;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.utils.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.backups.app.ui.Constants.MAX_PROGRESS;
import static com.backups.app.ui.Constants.MIN_PROGRESS;
import static com.backups.app.ui.Constants.PROGRESS_RATE;
import static com.backups.app.ui.Constants.REMOVE_FROM;

public class BackupRepository {
  public enum OutputStorage {
    MOUNTED,
    USING_DEFAULT_STORAGE,
    NO_MOUNTED_DEVICES,
    UNKNOWN_STORAGE_VOLUME
  }

  private static class StorageVolumeState {
    // anything greater than sPrimaryStorage is considered external storage
    // (sdcard, etc)
    private int storageVolume = -1;

    private int availableStorageVolumes = 0;

    private boolean hasSufficientStorage = false;

    private boolean storageVolumesAvailable = false;
  }

  public final static int sPrimaryStorage = 0;

  private final int mErrorCode = -1;

  private long mBackupSize = 0L;

  private final StorageVolumeState mStorageVolumeState =
      new StorageVolumeState();

  private File mOutputDirectory;

  private final File[] mExternalStorageVolumes;

  private final Executor mExecutor = Executors.newSingleThreadExecutor();

  public BackupRepository(Context context) {
    mExternalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null);
    mStorageVolumeState.availableStorageVolumes =
        availableOutputDirectories(mExternalStorageVolumes);

    boolean storageVolumesAreAvailable =
        mStorageVolumeState.availableStorageVolumes != mErrorCode;

    if (storageVolumesAreAvailable) {
      mStorageVolumeState.storageVolumesAvailable = true;
      mOutputDirectory = mExternalStorageVolumes[sPrimaryStorage];
      mStorageVolumeState.storageVolume = sPrimaryStorage;
    } else {
      mStorageVolumeState.storageVolumesAvailable = false;
    }
  }

  private int availableOutputDirectories(File[] externalStorageVolumes) {
    int availableVolumes = 0;
    if (externalStorageVolumes == null) {
      availableVolumes = mErrorCode;
    } else if (externalStorageVolumes.length == 1) {
      availableVolumes = 1;
    } else {

      for (File volume : externalStorageVolumes) {
        boolean mounted = Environment.getExternalStorageState(volume).equals(
            Environment.MEDIA_MOUNTED);
        if (mounted) {
          ++availableVolumes;
        }
      }
    }
    return availableVolumes;
  }

  public boolean setStorageVolume(int selection) {
    boolean guard = !mStorageVolumeState.storageVolumesAvailable &&
                    selection > mExternalStorageVolumes.length;
    if (guard) {
      return false;
    }

    File outputTo = mExternalStorageVolumes[selection];

    boolean mounted = Environment.getExternalStorageState(outputTo).equals(
        Environment.MEDIA_MOUNTED);

    if (mounted) {
      mStorageVolumeState.storageVolume = selection;
      mOutputDirectory = outputTo;
    }

    return true;
  }

  public int getStorageVolumeIndex() {
    return mStorageVolumeState.storageVolume;
  }

  public String getStorageVolumePath() {
    return (mStorageVolumeState.storageVolumesAvailable
                ? mOutputDirectory.getAbsolutePath()
                : "");
  }

  public int getAvailableStorageVolumeCount() {
    return mStorageVolumeState.availableStorageVolumes;
  }

  public BackupRepository.OutputStorage
  isOutputDirectoryMounted(final int index) {
    if (index < mStorageVolumeState.availableStorageVolumes) {
      if (Environment.getExternalStorageState(mExternalStorageVolumes[index])
              .equals(Environment.MEDIA_MOUNTED)) {

        return BackupRepository.OutputStorage.MOUNTED;

      } else {
        boolean canUseDefaultStorage =
            Environment
                .getExternalStorageState(
                    mExternalStorageVolumes[sPrimaryStorage])
                .equals(Environment.MEDIA_MOUNTED);

        if (canUseDefaultStorage) {
          return BackupRepository.OutputStorage.USING_DEFAULT_STORAGE;
        }
      }

      return BackupRepository.OutputStorage.NO_MOUNTED_DEVICES;
    }

    return OutputStorage.UNKNOWN_STORAGE_VOLUME;
  }

  public void incrementBackupSize(final long by) { mBackupSize += by; }

  public void zeroBackupSize() { mBackupSize = 0L; }

  private void publishProgress(BackupProgress progressState,
                               Callback<BackupProgress> callback) {
    int progress = MIN_PROGRESS;

    progressState.setState(BackupProgress.ProgressState.ONGOING);
    progressState.setProgress(PROGRESS_RATE);

    int backupWaitTime = 175;

    while (MAX_PROGRESS >= progress) {
      progress += PROGRESS_RATE;

      try {
        Thread.sleep(backupWaitTime);
      } catch (InterruptedException interruptedException) {
        interruptedException.printStackTrace();
      }

      callback.onComplete(progressState);
    }
  }

  private void beginBackupProcess(List<APKFile> backups,
                                  Callback<BackupProgress> callback) {
    APKFile backup = backups.get(com.backups.app.ui.Constants.REMOVE_FROM);

    BackupProgress progress = new BackupProgress();

    progress.setBackupName(backup.getName());

    publishProgress(progress, callback);

    if (!createBackup(backup.getPackagePath(), backup.getBackupName())) {
      progress.setState(BackupProgress.ProgressState.ERROR);
    } else {
      progress.setState(BackupProgress.ProgressState.FINISHED);
    }

    backups.remove(REMOVE_FROM);

    callback.onComplete(progress);
  }

  public boolean hasSufficientStorage() {
    return (mStorageVolumeState.hasSufficientStorage =
                mOutputDirectory.getUsableSpace() > mBackupSize);
  }

  private boolean createBackup(final String src, final String dest) {
    boolean wasAbleToBackup = true;

    try (FileInputStream inStream = new FileInputStream(new File(src));
         FileOutputStream outStream =
             new FileOutputStream(new File(mOutputDirectory, dest));

         FileChannel inChannel = inStream.getChannel();
         FileChannel outChannel = outStream.getChannel()) {

      int from = 0;
      inChannel.transferTo(from, inChannel.size(), outChannel);

    } catch (IOException ioException) {
      ioException.printStackTrace();
      wasAbleToBackup = false;
    }
    return wasAbleToBackup;
  }

  public void backup(List<APKFile> backups, Callback<BackupProgress> callback) {
    boolean canBackup = mStorageVolumeState.hasSufficientStorage &&
                        mStorageVolumeState.storageVolumesAvailable &&
                        !backups.isEmpty();

    if (canBackup) {

      mExecutor.execute(() -> {
        boolean hasOnlyOneBackup = backups.size() == 1;

        BackupProgress began = new BackupProgress();

        began.setState(BackupProgress.ProgressState.BEGAN);

        callback.onComplete(began);

        if (hasOnlyOneBackup) {
          beginBackupProcess(backups, callback);
        } else {
          int total = backups.size();

          while (total != 0) {
            beginBackupProcess(backups, callback);
            --total;
          }
        }
      });
    }
  }
}
