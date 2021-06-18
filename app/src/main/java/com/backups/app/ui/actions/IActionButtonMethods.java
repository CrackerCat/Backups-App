package com.backups.app.ui.actions;

import com.backups.app.utils.IDefaultAction;

public interface IActionButtonMethods {
  void initializeActionButtonActions(final int position,
                                     final IActionButton action,
                                     final IDefaultAction defaultChecks);
}
