package com.backups.app.fileoperations;

import android.content.pm.PackageManager;

import com.backups.app.data.APKFile;

import java.util.ArrayList;

interface IAPKFileRepository {
    ArrayList<APKFile> getInstalledAppFiles(PackageManager packageManager);

    boolean makeBackups(ArrayList<APKFile> apps);
}
