package com.backups.app.data.pojos;

import android.graphics.drawable.Drawable;

public final class ApkFile {
  private boolean mIsSelected = false;
  private int mPackageHash = 0;
  private final String mName;
  private final String mPackageName;
  private final String mPackagePath;
  private final long mAppSize;
  private final Drawable mIcon;

  public ApkFile(final String name, final String packageName,
                 final String packagePath, final long appSize,
                 final Drawable icon) {
    mName = name;
    mPackageName = packageName;
    mPackagePath = packagePath;
    mAppSize = appSize;
    mIcon = icon;
  }

  @Override
  public String toString() {
    return "APKFile{"
        + "Name='" + mName + '\'' + ", PackageName='" + mPackageName + '\'' +
        ", PackagePath='" + mPackagePath + '\'' + ", AppSize=" + mAppSize +
        ", Icon=" + mIcon + '}';
  }

  public boolean marked() { return mIsSelected; }

  public int getPackageHash() {
    return (mPackageHash == 0 ? computePackageHash() : mPackageHash);
  }

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

  public boolean isDuplicate() { return mName.endsWith(")"); }

  private int computePackageHash() {
    mPackageHash = mPackagePath.hashCode() + mPackageName.hashCode();

    return mPackageHash;
  }
}
