package com.backups.app.data;

import android.graphics.drawable.Drawable;

public class APKFile {
    private final String mName;
    private final String mPackageName;
    private final String mPackagePath;
    private final long mAppSize;
    private final Drawable mIcon;

    APKFile(final String name, final String packageName, final String packagePath, final long appSize, final Drawable icon) {
        mName = name;
        mPackageName = packageName;
        mPackagePath = packagePath;
        mAppSize = appSize;
        mIcon = icon;
    }

    public String getName() {
        return mName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getPackagePath() {
        return mPackagePath;
    }

    public long getAppSize() {
        return mAppSize;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    @Override
    public String toString() {
        return "ApkFile {\n" +
                " name: " + mName +
                ",\n packageName: " + mPackageName +
                ",\n packagePath: " + mPackagePath +
                ",\n appSize: " + mAppSize +
                ",\n icon: " + mIcon +
                "\n}";
    }
}
