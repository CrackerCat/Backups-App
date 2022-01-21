package com.backups.app.data.viewmodels.appqueue;

import static com.backups.app.Constants.PRIMARY_STORAGE;

import android.content.Context;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import com.backups.app.data.pojos.ApkFile;
import java.io.File;
import java.util.List;

final class StorageVolumeHelper {
  private static final class StorageVolumeState {
    private boolean hasSufficientStorage = false;

    private boolean storageVolumesAvailable = false;

    public final static int STORAGE_ERROR = -1;

    // anything greater than Constants.PRIMARY_STORAGE is considered external
    // storage (sdcard, etc)
    private int storageVolume = STORAGE_ERROR;

    private int availableStorageVolumes = 0;
  }

  private final StorageVolumeState mStorageVolumeState =
      new StorageVolumeState();

  private File mOutputDirectory = null;

  private final File[] mExternalStorageVolumes;

  StorageVolumeHelper(final Context context) {
    mExternalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null);

    setupInitialStorageVolume();
  }
  public boolean isMounted(final int volume) {
    return Environment.getExternalStorageState(mOutputDirectory).equals(Environment.MEDIA_MOUNTED);
  }

  public boolean setStorageVolume(final int selection) {
    if (!mStorageVolumeState.storageVolumesAvailable &&
        selection > mExternalStorageVolumes.length) {
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

  public boolean hasSufficientStorage(final long backupSize) {
    return (mStorageVolumeState.hasSufficientStorage =
                mOutputDirectory.getUsableSpace() > backupSize);
  }

  public boolean canBackup(final List<ApkFile> backups) {
    return (mStorageVolumeState.hasSufficientStorage &&
            mStorageVolumeState.storageVolumesAvailable && !backups.isEmpty());
  }

  public Pair<String[], String[]>
  makeStorageEntryValues(final String primaryStorageFmt,
                         final String externalStorageFmt) {
    final int volumesAvailable = mStorageVolumeState.availableStorageVolumes;

    final String[] entries = new String[volumesAvailable];

    final String[] values = new String[volumesAvailable];

    final int primaryStorage = 0;

    entries[primaryStorage] = primaryStorageFmt;

    values[primaryStorage] = PRIMARY_STORAGE + "";

    final boolean foundExternalDrives = volumesAvailable != 1;

    if (foundExternalDrives) {
      // storageVolumeIndex = 1 to account for only external volumes
      for (int storageVolumeIndex = 1; storageVolumeIndex < volumesAvailable;
           ++storageVolumeIndex) {
        entries[storageVolumeIndex] =
            String.format(externalStorageFmt, storageVolumeIndex);

        values[storageVolumeIndex] = storageVolumeIndex + "";
      }
    }

    return new Pair<>(entries, values);
  }

  private int availableOutputDirectories() {
    int availableVolumes = 0;

    if (mExternalStorageVolumes == null) {
      availableVolumes = StorageVolumeState.STORAGE_ERROR;

    } else if (mExternalStorageVolumes.length == 1) {
      availableVolumes = 1;
    } else {

      for (final File volume : mExternalStorageVolumes) {

        if (Environment.getExternalStorageState(volume).equals(
                Environment.MEDIA_MOUNTED)) {
          ++availableVolumes;
        }
      }
    }

    return availableVolumes;
  }

  private void setupInitialStorageVolume() {
    final int availableVolumes = availableOutputDirectories();

    if (availableVolumes > 0) {
      mStorageVolumeState.storageVolumesAvailable = true;

      mStorageVolumeState.storageVolume = PRIMARY_STORAGE;

      mStorageVolumeState.availableStorageVolumes = availableVolumes;

      mOutputDirectory = mExternalStorageVolumes[PRIMARY_STORAGE];
    }
  }
}
