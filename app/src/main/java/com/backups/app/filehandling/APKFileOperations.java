package com.backups.app.filehandling;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class APKFileOperations {

    public static final String OUTPUT_DIRECTORY = "Backups";
    public static final int CREATE_DIRECTORY_CODE = 51;
    public static boolean DISPLAY_SYSTEM_APPS = false;

    private static boolean isSystemApp(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public static ArrayList<AppFile> getInstalledAppFiles(Context context) {
        ArrayList<AppFile> installedApps = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            if (!isSystemApp(applicationInfo) && !DISPLAY_SYSTEM_APPS || isSystemApp(applicationInfo) && DISPLAY_SYSTEM_APPS) {

                String name = applicationInfo.loadLabel(packageManager).toString();
                String packageName = applicationInfo.packageName;
                String apkPath = applicationInfo.sourceDir;
                long apkSize = new File(applicationInfo.sourceDir).length();
                Drawable icon = packageManager.getApplicationIcon(applicationInfo);

                installedApps.add(new AppFile(name, packageName, apkPath, apkSize, icon));
            }
        }

        return installedApps;
    }

    public static boolean makeBackups(ArrayList<AppFile> apps) {
        return false;
    }
}
