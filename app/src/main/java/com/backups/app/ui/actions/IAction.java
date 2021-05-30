package com.backups.app.ui.actions;

import android.view.View;

public interface IAction {
  void assignCallBacks(View.OnClickListener activeCallback,
                       View.OnClickListener inactiveCallback);

  void inactive();

  void active();

  void setAvailability(final boolean flag);

  boolean getAvailability();

  void display(final boolean flag);

  boolean canBeDisplayed();
}
