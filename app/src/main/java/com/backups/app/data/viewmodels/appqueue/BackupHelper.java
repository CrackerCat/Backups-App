package com.backups.app.data.viewmodels.appqueue;

import static com.backups.app.Constants.MAX_PROGRESS;
import static com.backups.app.Constants.MIN_PROGRESS;
import static com.backups.app.Constants.PROGRESS_RATE;
import static com.backups.app.Constants.REMOVE_FROM;

import com.backups.app.data.pojos.ApkFile;
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

final class BackupHelper {

  private long mBackupSize = 0L;

  private String mOutputDirectory;

  private final Executor mExecutor = Executors.newSingleThreadExecutor();

  public long getBackupSize() { return mBackupSize; }

  public void incrementBackupSize(final long by) { mBackupSize += by; }

  public void zeroBackupSize() { mBackupSize = 0L; }

  public BackupHelper(final String outputDirectory) {
    mOutputDirectory = outputDirectory;
  }

  public void changeOutputDirectory(final String outputDirectory) {
    mOutputDirectory = outputDirectory;
  }

  public void backup(final List<ApkFile> backups,
                     final Callback<BackupProgress> callback) {

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
      } catch (final InterruptedException interruptedException) {
        interruptedException.printStackTrace();
      }

      callback.invoke(progressState);
    }
  }

  private BackupProgress setupBackupProgress(final ApkFile backup) {
    final BackupProgress progress = new BackupProgress();

    progress.setBackupHash(backup.getPackageHash());

    progress.setBackupName(backup.getName());

    return progress;
  }

  private void attemptBackup(final BackupProgress progress,
                             final ApkFile backup,
                             Callback<BackupProgress> callback) {
    publishProgress(progress, callback);

    if (!createBackup(backup.getPackagePath(), backup.getBackupName())) {
      progress.setState(BackupProgress.ProgressState.ERROR);
    } else {
      progress.setState(BackupProgress.ProgressState.FINISHED);
    }
  }

  private void beginBackupProcess(List<ApkFile> backups,
                                  Callback<BackupProgress> callback) {

    final ApkFile backup = backups.get(REMOVE_FROM);

    final BackupProgress backupProgress = setupBackupProgress(backup);

    attemptBackup(backupProgress, backup, callback);

    backups.remove(REMOVE_FROM);

    callback.invoke(backupProgress);
  }

  private boolean createBackup(final String sourcePath,
                               final String destinationPath) {
    boolean wasAbleToBackup = true;

    try (final FileInputStream inStream = new FileInputStream(sourcePath);
         final FileOutputStream outStream =
             new FileOutputStream(new File(mOutputDirectory, destinationPath));

         FileChannel inChannel = inStream.getChannel();
         FileChannel outChannel = outStream.getChannel()) {

      int from = 0;
      inChannel.transferTo(from, inChannel.size(), outChannel);

    } catch (final IOException ioException) {
      ioException.printStackTrace();
      wasAbleToBackup = false;
    }
    return wasAbleToBackup;
  }
}
