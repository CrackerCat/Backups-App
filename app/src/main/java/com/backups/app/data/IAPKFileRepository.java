package com.backups.app.data;

import android.content.pm.PackageManager;

import java.util.ArrayList;

interface IAPKFileRepository {
    ArrayList<APKFile> getInstalledApps(PackageManager packageManager);

}
