package com.backups.app.data.repositories;

import androidx.lifecycle.MutableLiveData;
import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.viewmodels.DataEvent;
import com.backups.app.utils.PackageNameUtils;
import java.util.ArrayList;
import java.util.List;

public class AppQueueRepository {
  private DataEvent mLastDataEvent = DataEvent.NONE;

  private final List<APKFile> mSelectedItems = new ArrayList<>();

  private final List<APKFile> mAppsToBackup = new ArrayList<>();

  private final MutableLiveData<DataEvent> mDataEventLiveData =
      new MutableLiveData<>();

  public MutableLiveData<DataEvent> getDataEventLiveData() {
    return mDataEventLiveData;
  }

  public DataEvent getLastDataEvent() { return mLastDataEvent; }

  public List<APKFile> getSelectedItems() { return mSelectedItems; }

  public List<APKFile> getAppsInQueue() { return mAppsToBackup; }

  public void addAppToQueue(final APKFile backup) {
    String selectedAPKName = backup.getName();

    String repeatedBackupName =
        PackageNameUtils.computeRepeatedBackupName(selectedAPKName);

    if (repeatedBackupName != null) {
      APKFile repeat = new APKFile(repeatedBackupName, backup.getPackageName(),
                                   backup.getPackagePath(), backup.getAppSize(),
                                   backup.getIcon());
      mAppsToBackup.add(repeat);
    } else {
      mAppsToBackup.add(backup);
    }

    mDataEventLiveData.setValue(
        (mLastDataEvent = DataEvent.ITEM_ADDED_TO_QUEUE));

    mLastDataEvent = DataEvent.NONE;
  }

  public void addOrRemoveSelection(APKFile item) {
    if (!mSelectedItems.contains(item)) {
      mSelectedItems.add(item);

      mDataEventLiveData.setValue((mLastDataEvent = DataEvent.ITEM_SELECTED));

    } else if (mSelectedItems.remove(item)) {

      mDataEventLiveData.setValue((mLastDataEvent = DataEvent.ITEM_DESELECTED));
    }

    mLastDataEvent = DataEvent.NONE;
  }

  public void selectAll() {
    if (!mAppsToBackup.isEmpty()) {

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION));

      mSelectedItems.clear();

      for (APKFile app : mAppsToBackup) {
        app.mark(true);

        mSelectedItems.add(app);
      }

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ALL_ITEMS_SELECTED));
    }

    mLastDataEvent = DataEvent.NONE;
  }

  public void clearAndEmptySelection() {
    if (!mSelectedItems.isEmpty()) {

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION));

      for (APKFile app : mSelectedItems) {
        if (app.marked()) {
          app.mark(false);

          PackageNameUtils.resetCountFor(app.getName());

          mAppsToBackup.remove(app);
        }
      }

      mSelectedItems.clear();

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ITEMS_REMOVED_FROM_SELECTION));
    }

    mLastDataEvent = DataEvent.NONE;
  }

  public void clearSelection() {
    if (!mSelectedItems.isEmpty()) {
      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION));

      for (APKFile app : mSelectedItems) {
        app.mark(false);
      }

      mSelectedItems.clear();

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ALL_ITEMS_DESELECTED));
    }

    mLastDataEvent = DataEvent.NONE;
  }

  public void emptyQueue() {
    if (!mAppsToBackup.isEmpty()) {

      if (!mSelectedItems.isEmpty()) {
        mDataEventLiveData.setValue(
            (mLastDataEvent = DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION));

        for (APKFile app : mSelectedItems) {
          app.mark(false);
        }

        mSelectedItems.clear();
      }

      mAppsToBackup.clear();

      PackageNameUtils.clearRepeatedNameTable();

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ITEMS_REMOVED_FROM_QUEUE));

      mLastDataEvent = DataEvent.NONE;
    }
  }
}
