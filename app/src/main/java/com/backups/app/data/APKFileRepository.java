package com.backups.app.data;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class APKFileRepository implements IAPKFileRepository {

    private static final String OUTPUT_DIRECTORY = "Backups";
    private static boolean mDisplaySystemApps = false;
    private final Executor mExecutor;

    private static boolean isSystemApp(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public APKFileRepository(Executor executor) {
        mExecutor = executor;
    }

    public static String getOutputDirectory() {
        return OUTPUT_DIRECTORY;
    }

    public static void willDisplaySystemApps(boolean choice) {
        mDisplaySystemApps = choice;
    }

    public static boolean displaySystemApps() {
        return mDisplaySystemApps;
    }

    public interface Callback<T> {
        void onComplete(T result);
    }

    public void deliverInstalledApps(PackageManager packageManager, Callback<ArrayList<APKFile>> callback) {
        mExecutor.execute(() -> {
            ArrayList<APKFile> apkFiles = getInstalledApps(packageManager);
            callback.onComplete(apkFiles);
        });
    }


    @Override
    public ArrayList<APKFile> getInstalledApps(PackageManager packageManager) {
        ArrayList<APKFile> installedApps = new ArrayList<>();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            if (!isSystemApp(applicationInfo) && !mDisplaySystemApps || isSystemApp(applicationInfo) && mDisplaySystemApps) {

                String name = applicationInfo.loadLabel(packageManager).toString();
                String packageName = applicationInfo.packageName;
                String apkPath = applicationInfo.sourceDir;
                long apkSize = new File(applicationInfo.sourceDir).length();
                Drawable icon = packageManager.getApplicationIcon(applicationInfo);

                installedApps.add(new APKFile(name, packageName, apkPath, apkSize, icon));
            }
        }
        return installedApps;
    }

    @Override
    public boolean makeBackups(ArrayList<APKFile> apps) {
        return false;
    }

}
