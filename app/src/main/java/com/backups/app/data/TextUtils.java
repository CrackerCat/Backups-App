package com.backups.app.data;

public class TextUtils {
    private static final String[] prefixes = {"com", "org"};

    public static boolean containsPackageNamePrefix(final String appName) {
        boolean result = false;
        for (String prefix :
                prefixes) {
            result = (appName.startsWith(prefix));
            if (result) {
                break;
            }
        }
        return result;
    }

    public static String extractHumanReadableName(final String appName) {

        int end = appName.length() - 1;

        while (appName.charAt(end) != '.') {
            --end;
        }

        int indexOfFirstLetter = end + 1;
        int indexAfterFirstLetter = indexOfFirstLetter + 1;

        char firstLetterOfName = Character.toUpperCase(appName.charAt(indexOfFirstLetter));

        return firstLetterOfName +
                appName.substring(indexAfterFirstLetter);
    }
}
