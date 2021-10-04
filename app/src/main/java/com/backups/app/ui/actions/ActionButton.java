package com.backups.app.ui.actions;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
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

  public static class Config {
    IPresenter presenter;
    FragmentActivity activity;
    @IdRes int[] layoutIDs;
    int[] colors;
    boolean active;

    public Config(final IPresenter presenter, final FragmentActivity activity,
                  @IdRes final int[] layoutIDs, final int[] colors,
                  final boolean active) {
      this.activity = activity;
      this.presenter = presenter;
      this.layoutIDs = layoutIDs;
      this.colors = colors;
      this.active = active;
    }
  }

  public ActionButton(final Config config) {

    mActionLabel = config.activity.findViewById(config.layoutIDs[0]);

    mActionButton = config.activity.findViewById(config.layoutIDs[1]);

    mInternalState.activeColor = config.colors[0];

    mInternalState.inactiveColor = config.colors[1];

    mInternalState.isActive = config.active;

    if (mActionLabel != null && mActionButton != null) {
      mInternalState.hasAssignedViews = true;
      mParent = config.presenter;
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
    if (!(mInternalState.isActive = flag)) {
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