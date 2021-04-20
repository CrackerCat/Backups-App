package com.backups.app.data;

import android.content.Context;
import android.os.Environment;

import androidx.core.content.ContextCompat;

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

public class BackupCreator implements IBackups {
  public final static int sPrimaryStorage = 0;

  public final static int sExternalStorage = 1;

  private final static int sBackupWaitTime = 235;

  // anything greater than sPrimaryStorage is considered external storage
  // (sdcard, etc)
  private int mStorageVolume;

  private final int mAvailableStorageVolumes;

  private boolean mHasSufficientStorage = false;

  private boolean mIsBackupInProgress = false;

  private final boolean mStorageVolumesAvailable;

  private File mOutputDirectory;

  private final File[] mExternalStorageVolumes;

  private final Executor mExecutor = Executors.newSingleThreadExecutor();

  public BackupCreator(Context context) {
    mExternalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null);
    mAvailableStorageVolumes =
        availableOutputDirectories(mExternalStorageVolumes);

    boolean storageVolumesAreAvailable = mAvailableStorageVolumes != -1;
    if (storageVolumesAreAvailable) {
      mStorageVolumesAvailable = true;
      mOutputDirectory = mExternalStorageVolumes[sPrimaryStorage];
      mStorageVolume = sPrimaryStorage;
    } else {
      mStorageVolumesAvailable = false;
    }
  }

  private boolean isExternalStorageWritable(File directory) {
    // used to check if storage is available
    return Environment.getExternalStorageState(directory).equals(
        Environment.MEDIA_MOUNTED);
  }

  private int availableOutputDirectories(File[] externalStorageVolumes) {
    int availableVolumes = 0;
    if (externalStorageVolumes == null) {
      availableVolumes = -1;
    } else if (externalStorageVolumes.length == 1) {
      availableVolumes = 1;
    } else {
      for (File volume : externalStorageVolumes) {
        if (isExternalStorageWritable(volume)) {
          ++availableVolumes;
        }
      }
    }
    return availableVolumes;
  }

  public int setStorageVolume(int selection) {
    File outputTo = null;
    if (selection == sPrimaryStorage) {
      return selection;
    } else {
      if (selection < mExternalStorageVolumes.length) {
        outputTo = mExternalStorageVolumes[selection];
      }
    }

    if (outputTo != null) {
      if (!isExternalStorageWritable(outputTo)) {
        selection = -1;
      } else {
        mStorageVolume = selection;
        mOutputDirectory = outputTo;
      }
    } else {
      selection = -1;
    }

    return selection;
  }

  public int getStorageVolumeIndex() { return mStorageVolume; }

  public int getAvailableStorageVolumeCount() {
    return mAvailableStorageVolumes;
  }

  private void publishProgress(APKFile backup, BackupProgress progressState,
                               Callback<BackupProgress> callback) {
    int progress = MIN_PROGRESS;

    progressState.state = BackupProgress.ProgressState.ONGOING;
    progressState.progress = PROGRESS_RATE;

    while (progress != MAX_PROGRESS) {
      progress += PROGRESS_RATE;

      try {
        Thread.sleep(sBackupWaitTime);
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
    progress.backupName = backup.getName();

    publishProgress(backup, progress, callback);

    if (!createBackup(backup.getPackagePath(), backup.getBackupName())) {
      progress.state = BackupProgress.ProgressState.ERROR;
    } else {
      progress.state = BackupProgress.ProgressState.FINISHED;
    }

    backups.remove(REMOVE_FROM);

    callback.onComplete(progress);
  }

  @Override
  public boolean hasSufficientStorage(long backupSize) {
    mHasSufficientStorage = mOutputDirectory.getUsableSpace() > backupSize;
    return mHasSufficientStorage;
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

  @Override
  public void backup(List<APKFile> backups, Callback<BackupProgress> callback) {
    synchronized (this) {

      boolean canBackup = mHasSufficientStorage && mStorageVolumesAvailable &&
                          !backups.isEmpty();

      if (canBackup) {

        mExecutor.execute(() -> {
          boolean hasOnlyOneBackup = backups.size() == 1;
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
}
