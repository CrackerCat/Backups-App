package com.backups.app.fileoperations;

import android.graphics.drawable.Drawable;

public class APKFile {
    private final String m_Name;
    private final String m_PackageName;
    private final String m_PackagePath;
    private final long m_AppSize;
    private final Drawable m_Icon;

    APKFile(final String name, final String packageName, final String packagePath, final long appSize, final Drawable icon) {
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
        return "ApkFile {\n" +
                " name: " + m_Name +
                ",\n packageName: " + m_PackageName +
                ",\n packagePath: " + m_PackagePath +
                ",\n appSize: " + m_AppSize +
                ",\n icon: " + m_Icon +
                "\n}";
    }
}
