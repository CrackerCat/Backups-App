package com.backups.app.data;

import android.content.pm.PackageManager;

import java.util.List;

interface IAPKFileRepository {
   void fetchInstalledApps(final PackageManager packageManager, final Callback<List<APKFile>> callback);
   void fetchSearchResult(final String query, final List<APKFile> apps, final Callback<List<APKFile>> callback);
}
