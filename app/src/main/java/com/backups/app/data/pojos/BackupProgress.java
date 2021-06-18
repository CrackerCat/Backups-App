package com.backups.app.data.pojos;

public class BackupProgress {
  public enum ProgressState { NONE, BEGAN, ONGOING, FINISHED, ERROR, ENDED }

  private ProgressState mState = ProgressState.NONE;
  private String mBackupName = null;
  private int mProgress = 0;

  public ProgressState getState() { return mState; }

  public void setState(ProgressState mState) { this.mState = mState; }

  public String getBackupName() { return mBackupName; }

  public void setBackupName(String mBackupName) {
    this.mBackupName = mBackupName;
  }

  public int getProgress() { return mProgress; }

  public void setProgress(int mProgress) { this.mProgress = mProgress; }
}
