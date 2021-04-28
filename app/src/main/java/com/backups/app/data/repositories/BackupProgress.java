package com.backups.app.data;

public class BackupProgress {
  public enum ProgressState { NONE, ONGOING, FINISHED, ERROR }

  public ProgressState state = ProgressState.NONE;
  public String backupName;
  public int progress = 0;
}
