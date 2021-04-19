package com.backups.app.data;

import com.backups.app.utils.Callback;
import java.util.List;

public interface IBackups {
  boolean hasSufficientStorage(final long backupSize);
  void backup(List<APKFile> backups, Callback<BackupProgress> callback);
}
