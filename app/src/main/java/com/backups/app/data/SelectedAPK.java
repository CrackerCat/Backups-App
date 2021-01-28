package com.backups.app.data;

public class SelectedAPK {
    private final String mAppName;
    private final long mAppSize;

    public SelectedAPK(final String appName, final long appSize) {
        mAppName = appName;
        mAppSize = appSize;
    }

    public final String getAppName() {
        return mAppName;
    }

    public final long getAppSize() {
        return mAppSize;
    }
}
