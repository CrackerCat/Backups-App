package com.backups.app.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;
import java.util.Queue;

public class AppQueueViewModel {
    private final Queue<SelectedAPK> mSelectedApps;
    private final MutableLiveData<Queue<SelectedAPK>> mAppQueue;

    public AppQueueViewModel() {
        mSelectedApps = new LinkedList<>();
        mAppQueue = new MutableLiveData<>();
        mAppQueue.setValue(mSelectedApps);
    }

    public void push(SelectedAPK selectedAPK) {
        mSelectedApps.add(selectedAPK);
    }

    public SelectedAPK pop() {
        return mSelectedApps.remove();
    }

    public LiveData<Queue<SelectedAPK>> loadSelectedApps() {
        return mAppQueue;
    }
}
