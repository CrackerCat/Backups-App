package com.backups.app.ui.actions;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;

import com.backups.app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ActionButton implements IAction {

    private final TextView mActionLabel;
    private final FloatingActionButton mActionButton;
    private final int mInactiveColor;
    private boolean mIsActive;
    private boolean mHasAssignedView = false;
    private boolean mHasCallback = false;

    public ActionButton(final FragmentActivity activity,
                        final @IdRes int[] layoutIDs,
                        final boolean active) {

        mActionLabel = activity.findViewById(layoutIDs[0]);
        mActionButton = activity.findViewById(layoutIDs[1]);
        mInactiveColor = activity.getResources().getColor(R.color.primaryDarkColor);

        if (mActionLabel != null && mActionButton != null) {
            mHasAssignedView = true;
            mIsActive = active;
        }

    }

    @Override
    public void assignCallback(View.OnClickListener callback) {
        mActionButton.setOnClickListener(callback);
        mHasCallback = true;
    }

    @Override
    public void inactive() {
        mActionLabel.setVisibility(View.INVISIBLE);
        mActionButton.setBackgroundTintList(ColorStateList.valueOf(mInactiveColor));
    }

    @Override
    public void availability(boolean flag) {
        mIsActive = flag;
    }

    @Override
    public void display(boolean on) {
        if (!on) {
            mActionLabel.setVisibility(View.INVISIBLE);
            mActionButton.hide();
        } else {
            if (!mIsActive) {
                inactive();
            } else {
                mActionLabel.setVisibility(View.VISIBLE);
            }
            mActionButton.show();
        }
    }

    @Override
    public boolean canBeDisplayed() {
        return (mHasCallback && mHasAssignedView);
    }
}
