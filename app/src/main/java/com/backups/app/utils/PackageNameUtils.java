package com.backups.app.utils;

public final class PackageNameUtils {
  private PackageNameUtils() {}

  public static boolean containsPackageNamePrefix(final String appName) {
    final String[] packagePrefixes = {"com.", "org."};

    for (final String prefix : packagePrefixes) {
      if (appName.contains(prefix)) {
        return true;
      }
    }
    return false;
  }

  public static String extractReadableName(final String appName) {
    int end = appName.length() - 1;

    while (appName.charAt(end) != '.') {
      --end;
    }

    int indexOfFirstLetter = end + 1;
    int indexAfterFirstLetter = indexOfFirstLetter + 1;

    char firstLetterOfName = appName.charAt(indexOfFirstLetter);

    if (Character.isUpperCase(firstLetterOfName)) {
      firstLetterOfName = Character.toUpperCase(firstLetterOfName);
    }

    return firstLetterOfName + appName.substring(indexAfterFirstLetter);
  }
}
