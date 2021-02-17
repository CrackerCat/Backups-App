package com.backups.app.data;

public class PackageNameUtils {
  private static final String[] prefixes = {"com.", "org."};

  public static boolean containsPackageNamePrefix(final String appName) {
    for (String prefix : prefixes) {
      if (appName.contains(prefix)) {
        return true;
      }
    }
    return false;
  }

  public static String extractHumanReadableName(final String appName) {
    int end = appName.length() - 1;

    while (appName.charAt(end) != '.') {
      --end;
    }

    int indexOfFirstLetter = end + 1;
    int indexAfterFirstLetter = indexOfFirstLetter + 1;

    char firstLetterOfName = appName.charAt(indexOfFirstLetter);

    if(Character.isUpperCase(firstLetterOfName)) {
      firstLetterOfName =
              Character.toUpperCase(firstLetterOfName);
    }

    return firstLetterOfName + appName.substring(indexAfterFirstLetter);
  }
}
