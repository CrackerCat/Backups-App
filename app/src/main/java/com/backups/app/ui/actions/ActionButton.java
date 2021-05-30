package com.backups.app.ui.actions;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
import com.backups.app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ActionButton implements IAction {
  private static class ButtonState {
    public boolean isActive;
    public boolean hasAssignedViews = false;
    public int activeColor;
    public int inactiveColor;
  }

  private final ButtonState mInternalState = new ButtonState();

  private IPresenter mParent = null;
  private final TextView mActionLabel;
  private final FloatingActionButton mActionButton;

  public ActionButton(final IPresenter presenter,
                      final FragmentActivity activity,
                      final @IdRes int[] layoutIDs, final boolean active) {

    mActionLabel = activity.findViewById(layoutIDs[0]);

    mActionButton = activity.findViewById(layoutIDs[1]);

    mInternalState.activeColor =
        activity.getResources().getColor(R.color.secondaryDarkColor);

    mInternalState.inactiveColor =
        activity.getResources().getColor(R.color.primaryDarkColor);

    mInternalState.isActive = active;

    if (mActionLabel != null && mActionButton != null) {
      mInternalState.hasAssignedViews = true;
      mParent = presenter;
    }
  }

  @Override
  public void assignCallBacks(View.OnClickListener activeCallback,
                              View.OnClickListener inactiveCallback) {
    if (activeCallback != null && inactiveCallback != null) {
      mActionButton.setOnClickListener(v -> {
        if (!mInternalState.isActive) {
          inactiveCallback.onClick(v);
        } else {
          activeCallback.onClick(v);
        }
        mParent.hideActions();
      });
    }
  }

  @Override
  public void inactive() {
    mActionButton.setBackgroundTintList(
        ColorStateList.valueOf(mInternalState.inactiveColor));
  }

  @Override
  public void active() {
    mActionButton.setBackgroundTintList(
        ColorStateList.valueOf(mInternalState.activeColor));
  }

  @Override
  public void setAvailability(final boolean flag) {
    mInternalState.isActive = flag;

    if (!mInternalState.isActive) {
      inactive();
    } else {
      active();
    }
  }

  @Override
  public boolean getAvailability() {
    return mInternalState.isActive;
  }

  @Override
  public void display(final boolean on) {
    if (!on) {
      mActionLabel.setVisibility(View.GONE);

      mActionButton.hide();
    } else {
      mActionLabel.setVisibility(View.VISIBLE);

      mActionButton.show();
    }
  }

  @Override
  public boolean canBeDisplayed() {
    return mActionButton.hasOnClickListeners() &&
        mInternalState.hasAssignedViews;
  }
}