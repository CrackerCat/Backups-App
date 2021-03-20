package com.backups.app.ui.actions;

import android.view.View;

public interface IAction {
  void assignCallBacks(View.OnClickListener activeCallback,
                       View.OnClickListener inactiveCallback);

  void inactive();

  void active();

  void availability(boolean flag);

  boolean getAvailability();

  void display(boolean flag);

  boolean canBeDisplayed();
}
