package com.backups.app.data;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

public class APKFileRepository implements IAPKFileRepository {

    private final Executor mExecutor;
    private boolean mDisplaySystemApps = false;

    public APKFileRepository(final Executor executor) {
        mExecutor = executor;
    }

    private boolean isSystemApp(final ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public void displaySystemApps(boolean choice) {
        mDisplaySystemApps = choice;
    }

    private List<APKFile> getInstalledApps(final PackageManager packageManager) {
        LinkedList<APKFile> installedApps = new LinkedList<>();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            if (!isSystemApp(applicationInfo) && !mDisplaySystemApps || isSystemApp(applicationInfo) && mDisplaySystemApps) {

                String name = applicationInfo.loadLabel(packageManager).toString();

                String packageName;
                if (TextUtils.containsPackageNamePrefix(name)) {
                    packageName = name;
                    name = TextUtils.extractHumanReadableName(name);
                } else {
                    packageName = applicationInfo.packageName;
                }

                String apkPath = applicationInfo.sourceDir;
                long apkSize = new File(applicationInfo.sourceDir).length();
                Drawable icon = packageManager.getApplicationIcon(applicationInfo);

                installedApps.addLast(new APKFile(name, packageName, apkPath, apkSize, icon));
            }
        }

        Collections.sort(installedApps, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));

        return installedApps;
    }

    private List<APKFile> searchForIn(final String query, final List<APKFile> apps) {
        return null;
    }

    @Override
    public void fetchInstalledApps(final PackageManager packageManager, final Callback<List<APKFile>> callback) {
        mExecutor.execute(() -> {
            List<APKFile> apkFiles = getInstalledApps(packageManager);
            callback.onComplete(apkFiles);
        });
    }

    @Override
    public void fetchSearchResult(final String query, final List<APKFile> apps, final Callback<List<APKFile>> callback) {
        mExecutor.execute(() -> {
            List<APKFile> foundApps = searchForIn(query, apps);
            callback.onComplete(foundApps);
        });
    }
}
