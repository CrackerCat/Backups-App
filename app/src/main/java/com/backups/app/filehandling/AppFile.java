package com.backups.app.filehandling;

import android.graphics.drawable.Drawable;

public class AppFile {
    private final String m_Name;
    private final String m_PackageName;
    private final String m_PackagePath;
    private final long m_AppSize;
    private final Drawable m_Icon;

    AppFile(final String name, final String packageName, final String packagePath, final long appSize, final Drawable icon) {
        m_Name = name;
        m_PackageName = packageName;
        m_PackagePath = packagePath;
        m_AppSize = appSize;
        m_Icon = icon;
    }

    public String getName() {
        return m_Name;
    }

    public String getPackageName() {
        return m_PackageName;
    }

    public String getPackagePath() {
        return m_PackagePath;
    }

    public long getAppSize() {
        return m_AppSize;
    }

    public Drawable getIcon() {
        return m_Icon;
    }

    @Override
    public String toString() {
        return "AppFile{" +
                "m_Name='" + m_Name + '\'' +
                ", m_PackageName='" + m_PackageName + '\'' +
                ", m_PackagePath='" + m_PackagePath + '\'' +
                ", m_AppSize=" + m_AppSize +
                ", m_Icon=" + m_Icon +
                '}';
    }
}
