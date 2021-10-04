package com.backups.app.ui.actions;

public final class ActionSetMaker {
  private ActionSetMaker() {}

  private static IAction[] setupActions(
      final ActionButtonsConfig actionButtonConfig,
      final IActionSetFunctionality actionSetFunctionality,
      DefaultAction defaultAction) {
    final int total = actionButtonConfig.layoutIDs.length;

    final IAction[] actions = new IAction[total];

    for (int i = 0; i < total; ++i) {
      final ActionButton.Config config = new ActionButton.Config(
          actionButtonConfig.presenter, actionButtonConfig.parentActivity,
          actionButtonConfig.layoutIDs[i], actionButtonConfig.colors,
          actionButtonConfig.isActive);

      IAction action = new ActionButton(config);

      actionSetFunctionality.setup(i, action, defaultAction);

      actions[i] = action;
    }

    return actions;
  }

  public static IAction[] makeActionButtonSet(
      final ActionButtonsConfig actionButtonConfig,
      final IActionSetFunctionality actionSetFunctionality,
      final DefaultAction defaultAction) {

    final boolean canSetup = actionButtonConfig.layoutIDs != null &&
                             actionButtonConfig.presenter != null &&
                             actionButtonConfig.parentActivity != null;

    return (canSetup ? setupActions(actionButtonConfig, actionSetFunctionality,
                                    defaultAction)
                     : null);
  }
}
