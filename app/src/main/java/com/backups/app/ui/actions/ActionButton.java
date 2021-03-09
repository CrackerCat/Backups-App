package com.backups.app.ui.actions;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
import com.backups.app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ActionButton implements IAction {

  private View.OnClickListener mActiveCallback = null;
  private View.OnClickListener mInactiveCallback = null;
  private IPresenter mParent = null;
  private final TextView mActionLabel;
  private final FloatingActionButton mActionButton;
  private final int mActiveColor;
  private final int mInactiveColor;
  private boolean mIsActive;
  private boolean mHasAssignedViews = false;

  public ActionButton(final IPresenter presenter,
                      final FragmentActivity activity,
                      final @IdRes int[] layoutIDs, final boolean active) {

    mActionLabel = activity.findViewById(layoutIDs[0]);
    mActionButton = activity.findViewById(layoutIDs[1]);
    mActiveColor = activity.getResources().getColor(R.color.secondaryColor);
    mInactiveColor = activity.getResources().getColor(R.color.primaryDarkColor);
    mIsActive = active;

    if (mActionLabel != null && mActionButton != null) {
      mHasAssignedViews = true;
      mParent = presenter;
    }
  }

  private void updateState() {
    if (!mIsActive) {
      inactive();
    } else {
      active();
    }
  }

  @Override
  public void assignActiveCallback(View.OnClickListener callback) {
    mActiveCallback = callback;
  }

  @Override
  public void assignInactiveCallback(View.OnClickListener callback) {
    mInactiveCallback = callback;
  }

  @Override
  public void inactive() {
    if (mActionButton.isShown()) {
      mActionLabel.setVisibility(View.GONE);
      mActionButton.setBackgroundTintList(
          ColorStateList.valueOf(mInactiveColor));
    }
  }

  @Override
  public void active() {
    if (mActionButton.isShown()) {
      mActionLabel.setVisibility(View.VISIBLE);
      mActionButton.setBackgroundTintList(ColorStateList.valueOf(mActiveColor));
    }
  }

  @Override
  public void availability(boolean flag) {
    mIsActive = flag;
    updateState();
  }

  @Override
  public boolean getAvailablitiy() {
    return mIsActive;
  }

  @Override
  public void display(boolean on) {
    if (!on) {
      mActionLabel.setVisibility(View.GONE);
      mActionButton.hide();
    } else {
      mActionButton.show();
      updateState();
    }
  }

  @Override
  public boolean canBeDisplayed() {
    boolean canBeDisplayed = mActiveCallback != null &&
                             mInactiveCallback != null && mHasAssignedViews;

    if (canBeDisplayed) {
      mActionButton.setOnClickListener(v -> {
        if (!mIsActive) {
          mInactiveCallback.onClick(v);
        } else {
          mActiveCallback.onClick(v);
        }
        mParent.hideActions();
      });
    }

    return canBeDisplayed;
  }
}