package com.backups.app.ui.actionbuttonactions;

import android.view.View;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.backups.app.data.viewmodels.ApkListViewModel;
import com.backups.app.ui.actions.IActionButton;
import com.backups.app.ui.actions.IActionButtonMethods;
import com.backups.app.ui.actions.IDefaultAction;
import com.backups.app.ui.fragments.SearchDialogFragment;

import static com.backups.app.ui.Constants.SEARCH_BUTTON;

public final class AppListButtonActions implements IActionButtonMethods {
  private final FragmentActivity mParentActivity;

  private final ApkListViewModel mApkListViewModel;

  public AppListButtonActions(final FragmentActivity fragmentActivity) {
    mParentActivity = fragmentActivity;

    mApkListViewModel =
        new ViewModelProvider(fragmentActivity).get(ApkListViewModel.class);
  }

  @Override
  public void initializeActionButtonActions(int position, IActionButton action,
                                            IDefaultAction defaultChecks) {
    View.OnClickListener inactiveAction =
        (defaultChecks != null ? v -> defaultChecks.invoke() : v -> {});

    if (position == SEARCH_BUTTON) {

      action.assignCallBacks(v -> {
        SearchDialogFragment appSearchDialog = new SearchDialogFragment();

        appSearchDialog.setDataSetID(SearchDialogFragment.DataSet.APP_LIST);

        appSearchDialog.show(mParentActivity.getSupportFragmentManager(),
                             (appSearchDialog.getClass().getSimpleName()));
      }, inactiveAction);
    }
  }
}
