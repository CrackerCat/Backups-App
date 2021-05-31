package com.backups.app.utils;

import java.util.Hashtable;

public class PackageNameUtils {
  private PackageNameUtils() {}

  private static final String[] sPrefixes = {"com.", "org."};
  private static final String sRepeatedBackupFormatterString = "%s (%d)";

  private static final StringBuilder sBuffer = new StringBuilder();

  private static final Hashtable<Integer, Integer> sTimesTable =
      new Hashtable<>();

  public static boolean containsPackageNamePrefix(final String appName) {
    for (String prefix : sPrefixes) {
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

    if (Character.isUpperCase(firstLetterOfName)) {
      firstLetterOfName = Character.toUpperCase(firstLetterOfName);
    }

    return firstLetterOfName + appName.substring(indexAfterFirstLetter);
  }

  public static void resetCountFor(String appName) {
    int hash = appName.hashCode();
    sTimesTable.remove(hash);
  }

  public static String computeRepeatedBackupName(String appName) {
    String formattedName = null;
    int hash = appName.hashCode();

    if (sTimesTable.containsKey(hash)) {
      int count = sTimesTable.get(hash);

      sTimesTable.put(hash, ++count);

      sBuffer.append(
          String.format(sRepeatedBackupFormatterString, appName, count));

      formattedName = sBuffer.toString();
      sBuffer.delete(0, formattedName.length());
    } else {
      sTimesTable.put(hash, 0);
    }

    return formattedName;
  }

  public static void clearRepeatedNameTable() { sTimesTable.clear(); }
}
