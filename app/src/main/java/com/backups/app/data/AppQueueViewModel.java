package com.backups.app.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.LinkedList;
import java.util.Queue;

public class AppQueueViewModel extends ViewModel {
    private final Queue<SelectedAPK> mSelectedApps;
    private final MutableLiveData<Queue<SelectedAPK>> mAppQueue;
    private final StringBuffer mSelectedAppsBuffer;

    public AppQueueViewModel() {
        mSelectedApps = new LinkedList<>();

        mAppQueue = new MutableLiveData<>(mSelectedApps);

        int initialCapacity = 3;
        mSelectedAppsBuffer = new StringBuffer(initialCapacity);
    }

    private void updateBuffer() {
        mSelectedAppsBuffer.delete(0, mSelectedAppsBuffer.length());
        mSelectedAppsBuffer.append(mSelectedApps.size());
    }

    public void push(SelectedAPK selectedAPK) {
        mSelectedApps.add(selectedAPK);
        updateBuffer();
    }

    public SelectedAPK pop() {
        SelectedAPK top = mSelectedApps.remove();
        updateBuffer();
        return top;
    }

    public String getTotal() {
        return (mSelectedAppsBuffer.toString());
    }

    public LiveData<Queue<SelectedAPK>> getSelectedApps() {
        return mAppQueue;
    }
}
