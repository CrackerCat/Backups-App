package com.backups.app.ui.actions;

import android.view.View;

import androidx.annotation.IdRes;

public class ActionButtonConfig {
    private @IdRes
    int[] mLayoutIDS = null;
    private boolean mIsActive = true;
    private View.OnClickListener mClickListener = null;

    public ActionButtonConfig setLayoutIds(@IdRes int[] layoutIdList) {
        mLayoutIDS = layoutIdList;
        return this;
    }

    public ActionButtonConfig setAvailability(boolean on) {
        mIsActive = on;
        return this;
    }

    public ActionButtonConfig setCallBack(View.OnClickListener listener) {
        mClickListener = listener;
        return this;
    }

    public int[] getLayoutIDS() {
        return mLayoutIDS;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public View.OnClickListener getClickListener() {
        return mClickListener;
    }
}
