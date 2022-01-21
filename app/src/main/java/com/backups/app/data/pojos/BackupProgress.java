package com.backups.app.data.pojos;

public class BackupProgress {
  public enum ProgressState { NONE, BEGAN, ONGOING, FINISHED, ERROR, ENDED }

  private int mBackupHash = 0;
  private int mProgress = 0;
  private ProgressState mState = ProgressState.NONE;
  private String mBackupName = null;

  public int getBackupHash() { return mBackupHash; }

  public void setBackupHash(final int hash) { mBackupHash = hash; }

  public int getProgress() { return mProgress; }

  public void setProgress(int mProgress) { this.mProgress = mProgress; }

  public ProgressState getState() { return mState; }

  public void setState(ProgressState mState) { this.mState = mState; }

  public String getBackupName() { return mBackupName; }

  public void setBackupName(String mBackupName) {
    this.mBackupName = mBackupName;
  }

  public boolean finished() {
    return (mState == ProgressState.FINISHED || mState == ProgressState.ERROR);
  }
}
