package com.backups.app.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.LinkedList;
import java.util.Queue;

public class AppQueueViewModel extends ViewModel {
  private final Queue<APKFile> mSelectedApps = new LinkedList<>();
  private final MutableLiveData<Queue<APKFile>> mAppQueue =
      new MutableLiveData<>(mSelectedApps);

  public void push(APKFile selectedAPK) {
    mSelectedApps.add(selectedAPK);
  }

  public final APKFile pop() {
    return mSelectedApps.remove();
  }

  public int getSelectedAppCount() {return mSelectedApps.size();}

  public final LiveData<Queue<APKFile>> getSelectedApps() {
    return mAppQueue;
  }
}
