package com.backups.app.data.pojos;

import android.graphics.drawable.Drawable;

public class APKFile {
  private boolean mIsSelected = false;
  private final String mName;
  private final String mPackageName;
  private final String mPackagePath;
  private final long mAppSize;
  private final Drawable mIcon;

  public APKFile(final String name, final String packageName,
                 final String packagePath, final long appSize,
                 final Drawable icon) {
    mName = name;
    mPackageName = packageName;
    mPackagePath = packagePath;
    mAppSize = appSize;
    mIcon = icon;
  }

  public boolean marked() { return mIsSelected; }

  public String getName() { return mName; }

  public String getBackupName() { return mName + ".apk"; }

  public String getPackageName() { return mPackageName; }

  public String getPackagePath() { return mPackagePath; }

  public long getAppSize() { return mAppSize; }

  public Drawable getIcon() { return mIcon; }

  public boolean mark(final boolean selected) {
    mIsSelected = selected;
    return mIsSelected;
  }

  @Override
  public String toString() {
    return "APKFile{"
        + "isSelected=" + mIsSelected + ", name='" + mName +
        "'\n packageName='" + mPackageName + "'\n packagePath='" +
        mPackagePath + "'\n appSize=" + mAppSize + "'\n icon=" + mIcon + '}';
  }
}
