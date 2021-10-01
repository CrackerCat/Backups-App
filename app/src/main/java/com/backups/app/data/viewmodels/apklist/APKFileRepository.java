package com.backups.app.data.viewmodels.apklist;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.core.util.Pair;
import com.backups.app.data.pojos.ApkFile;
import com.backups.app.utils.Callback;
import com.backups.app.utils.PackageNameUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

final class APKFileRepository {

  private final Executor mExecutor;
  private boolean mDisplaySystemApps;

  public APKFileRepository(final Executor executor) { mExecutor = executor; }

  public void displaySystemApps(boolean choice) { mDisplaySystemApps = choice; }

  public void fetchInstalledApps(PackageManager packageManager,
                                 Callback<List<ApkFile>> callback) {
    mExecutor.execute(() -> {
      List<ApkFile> apkFiles = getInstalledApps(packageManager);
      callback.invoke(apkFiles);
    });
  }

  private boolean isSystemApp(final ApplicationInfo applicationInfo) {
    return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
  }

  private boolean extractDataFrom(ApplicationInfo applicationInfo) {
    return !isSystemApp(applicationInfo) && !mDisplaySystemApps ||
        isSystemApp(applicationInfo) && mDisplaySystemApps;
  }

  private Pair<String, String>
  getPackageNames(final ApplicationInfo applicationInfo,
                  final PackageManager packageManager) {
    String apkLabel = applicationInfo.loadLabel(packageManager).toString();

    String packageName;

    if (PackageNameUtils.containsPackageNamePrefix(apkLabel)) {
      packageName = apkLabel;
      apkLabel = PackageNameUtils.extractReadableName(apkLabel);
    } else {
      packageName = applicationInfo.packageName;
    }

    return new Pair<>(apkLabel, packageName);
  }

  private ApkFile extractApkData(final ApplicationInfo applicationInfo,
                                 final PackageManager packageManager) {
    final String apkPath = applicationInfo.sourceDir;
    final long apkSize = new File(apkPath).length();

    final Pair<String, String> apkNames =
        getPackageNames(applicationInfo, packageManager);

    return new ApkFile(apkNames.first, apkNames.second, apkPath, apkSize,
                       packageManager.getApplicationIcon(applicationInfo));
  }

  private List<ApkFile> getInstalledApps(final PackageManager packageManager) {
    final List<ApplicationInfo> packages =
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

    ArrayList<ApkFile> installedApps;

    if (!packages.isEmpty()) {
      installedApps = new ArrayList<>(packages.size());

      for (final ApplicationInfo applicationInfo : packages) {
        if (extractDataFrom(applicationInfo)) {

          installedApps.add(extractApkData(applicationInfo, packageManager));
        }
      }

      Collections.sort(installedApps,
                       (o1, o2)
                           -> o1.getName().toLowerCase().compareTo(
                               o2.getName().toLowerCase()));
    } else {
      installedApps = new ArrayList<>();
    }

    return installedApps;
  }
}
