package com.backups.app.data.viewmodels.appqueue;

import androidx.lifecycle.MutableLiveData;
import com.backups.app.data.events.DataEvent;
import com.backups.app.data.pojos.ApkFile;
import com.backups.app.utils.Callback;
import java.util.ArrayList;
import java.util.List;

final class AppQueueHelper {
  private DataEvent mLastDataEvent = DataEvent.NONE;

  private final List<ApkFile> mSelectedItems = new ArrayList<>();

  private final List<ApkFile> mAppsToBackup = new ArrayList<>();

  private final MutableLiveData<DataEvent> mDataEventLiveData =
      new MutableLiveData<>();

  public MutableLiveData<DataEvent> getDataEventLiveData() {
    return mDataEventLiveData;
  }

  public DataEvent getLastDataEvent() { return mLastDataEvent; }

  public List<ApkFile> getSelectedItems() { return mSelectedItems; }

  public List<ApkFile> getAppsInQueue() { return mAppsToBackup; }

  public void addAppToQueue(final ApkFile backup) {
    mAppsToBackup.add(backup);

    mDataEventLiveData.setValue(
        (mLastDataEvent = DataEvent.ITEM_ADDED_TO_QUEUE));

    mLastDataEvent = DataEvent.NONE;
  }

  public boolean addOrRemoveSelection(final ApkFile apkFile) {
    // TODO: refactor so that the index of the removed item is returned
    // REASON: to be able to update the RecyclerView efficiently, that is
    // without using notifyDataSetChanged

    boolean removedApk = false;

    if (!mSelectedItems.contains(apkFile)) {
      mSelectedItems.add(apkFile);

      mDataEventLiveData.setValue((mLastDataEvent = DataEvent.ITEM_SELECTED));

    } else if (mSelectedItems.remove(apkFile)) {
      removedApk = true;

      mDataEventLiveData.setValue((mLastDataEvent = DataEvent.ITEM_DESELECTED));
    }

    mLastDataEvent = DataEvent.NONE;

    return removedApk;
  }

  public void selectAll() {
    if (!mAppsToBackup.isEmpty()) {

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION));

      mSelectedItems.clear();

      for (final ApkFile app : mAppsToBackup) {
        app.mark(true);

        mSelectedItems.add(app);
      }

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ALL_ITEMS_SELECTED));
    }

    mLastDataEvent = DataEvent.NONE;
  }

  public void removeSelected(final Callback<ApkFile> duplicateApkAction) {
    // TODO: refactor so that the indices of the removed items are returned
    // REASON: to be able to update the RecyclerView efficiently, that is
    // without using notifyDataSetChanged

    if (!mSelectedItems.isEmpty()) {

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION));

      for (final ApkFile app : mSelectedItems) {
        if (app.marked()) {
          app.mark(false);

          if (app.isDuplicate()) {
            duplicateApkAction.invoke(app);
          }

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

      for (final ApkFile app : mSelectedItems) {
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

        for (ApkFile app : mSelectedItems) {
          app.mark(false);
        }

        mSelectedItems.clear();
      }

      mAppsToBackup.clear();

      mDataEventLiveData.setValue(
          (mLastDataEvent = DataEvent.ITEMS_REMOVED_FROM_QUEUE));

      mLastDataEvent = DataEvent.NONE;
    }
  }
}
