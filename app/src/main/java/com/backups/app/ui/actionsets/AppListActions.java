package com.backups.app.ui.actionsets;

import static com.backups.app.Constants.SEARCH_BUTTON;

import androidx.fragment.app.FragmentActivity;
import com.backups.app.ui.actions.DefaultAction;
import com.backups.app.ui.actions.IAction;
import com.backups.app.ui.actions.IActionSetFunctionality;
import com.backups.app.ui.fragments.SearchDialogFragment;

public final class AppListActions implements IActionSetFunctionality {
  private final FragmentActivity mParentActivity;

  public AppListActions(final FragmentActivity activity) {
    mParentActivity = activity;
  }

  @Override
  public void setup(final int position, IAction action,
                    DefaultAction defaultAction) {
    if (position == SEARCH_BUTTON) {
      searchForApp(action, defaultAction);
    }
  }

  private void searchForApp(final IAction action,
                            final DefaultAction defaultAction) {
    action.assignCallBacks(v -> {
      SearchDialogFragment appSearchDialog = new SearchDialogFragment();

      appSearchDialog.setDataSetID(SearchDialogFragment.DataSet.APP_LIST);

      appSearchDialog.show(mParentActivity.getSupportFragmentManager(),
                           (appSearchDialog.getClass().getSimpleName()));
    }, v -> defaultAction.invoke());
  }
}
