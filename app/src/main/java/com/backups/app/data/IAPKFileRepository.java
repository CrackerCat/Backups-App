package com.backups.app.data;

import android.content.pm.PackageManager;
import com.backups.app.utils.Callback;
import java.util.List;

interface IAPKFileRepository {
  void fetchInstalledApps(final PackageManager packageManager,
                          final Callback<List<APKFile>> callback);
}
