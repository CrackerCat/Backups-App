package com.backups.app.data;

public class BackupCreator implements IBackups {
  private static final String OUTPUT_DIRECTORY = "Backups";

  public static String getOutputDirectory() { return OUTPUT_DIRECTORY; }
}
