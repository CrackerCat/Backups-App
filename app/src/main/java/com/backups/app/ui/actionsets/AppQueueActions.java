package com.backups.app.ui.actionsets;

import static com.backups.app.Constants.APP_LIST;
import static com.backups.app.Constants.APP_QUEUE;
import static com.backups.app.Constants.BACKUP_BUTTON;
import static com.backups.app.Constants.ITEM_SELECTION_BUTTON;
import static com.backups.app.Constants.SEARCH_BUTTON;

import android.content.res.Resources;
import android.widget.Toast;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import com.backups.app.R;
import com.backups.app.data.events.SelectionState;
import com.backups.app.data.viewmodels.appqueue.AppQueueViewModel;
import com.backups.app.ui.actions.DefaultAction;
import com.backups.app.ui.actions.IAction;
import com.backups.app.ui.actions.IActionSetFunctionality;
import com.backups.app.ui.actions.IPresenter;
import com.backups.app.ui.fragments.SearchDialogFragment;

public final class AppQueueActions implements IActionSetFunctionality {
  private final FragmentActivity mParentActivity;

  private final AppQueueViewModel mAppQueueViewModel;

  private final IPresenter mActionPresenter;

  public AppQueueActions(final FragmentActivity activity,
                         final AppQueueViewModel appQueueViewModel,
                         final IPresenter presenter) {
    mParentActivity = activity;
    mAppQueueViewModel = appQueueViewModel;
    mActionPresenter = presenter;
  }

  @Override
  public void setup(int position, IAction action, DefaultAction defaultAction) {
    if (position == SEARCH_BUTTON) {
      action.assignCallBacks(v -> searchForApp(), v -> defaultAction.invoke());

    } else if (position == BACKUP_BUTTON) {
      action.assignCallBacks(v -> backupApps(), v -> defaultAction.invoke());

    } else if (position == ITEM_SELECTION_BUTTON) {
      action.assignCallBacks(
          v -> removeSelectedBackups(), v -> startBackupSelection());
    }
  }

  private void interruptSelectionState() {
    if (mAppQueueViewModel.getCurrentSelectionState() ==
        SelectionState.SELECTION_STARTED) {

      if (mAppQueueViewModel.hasSelectedItems()) {
        mAppQueueViewModel.clearSelection();

        mAppQueueViewModel.setItemSelectionStateTo(
            SelectionState.SELECTION_ENDED);
      }
    }
  }

  private void makeAppQueueActionsAvailable() {
    mActionPresenter.available(APP_QUEUE, SEARCH_BUTTON, false);
    mActionPresenter.available(APP_QUEUE, BACKUP_BUTTON, false);
  }

  private Pair<Boolean, String> startPreBackupChecks() {
    String outputMessage;

    boolean canStartBackup = false;

    final Resources resources = mParentActivity.getResources();

    if (mAppQueueViewModel.isBackupInProgress()) {
      outputMessage = resources.getString(R.string.backup_in_progress_message);

    } else if (!mAppQueueViewModel.hasSufficientStorage()) {
      outputMessage =
          resources.getString(R.string.insufficient_storage_message);

    } else {

      final String startingBackupMessage =
          resources.getString(R.string.commencing_backup_message);

      outputMessage = String.format(startingBackupMessage,
                                    mAppQueueViewModel.getAppsInQueue().size());

      canStartBackup = true;
    }

    return (new Pair<>(canStartBackup, outputMessage));
  }

  private void searchForApp() {
    final SearchDialogFragment appSearchDialog = new SearchDialogFragment();

    appSearchDialog.setDataSetID(SearchDialogFragment.DataSet.APP_QUEUE);

    appSearchDialog.show(mParentActivity.getSupportFragmentManager(),
                         (appSearchDialog.getClass().getSimpleName()));
  }

  private void backupApps() {
    final Pair<Boolean, String> status = startPreBackupChecks();

    if (status.first) {
      Toast.makeText(mParentActivity, status.second, Toast.LENGTH_SHORT).show();

      interruptSelectionState();

      mActionPresenter.available(APP_LIST, SEARCH_BUTTON, false);

      makeAppQueueActionsAvailable();

      mAppQueueViewModel.startBackup();
    }
  }

  private void displayBackupErrorMessage(final boolean backupNotInProgress) {
    final Resources resources = mParentActivity.getResources();

    if (backupNotInProgress) {
      Toast
          .makeText(mParentActivity,
                    resources.getString(R.string.no_apps_in_queue),
                    Toast.LENGTH_SHORT)
          .show();
    } else {
      Toast
          .makeText(
              mParentActivity,
              resources.getString(R.string.backup_in_progress_message_alt),
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void startBackupSelection() {
    final boolean backupNotInProgress =
        !mAppQueueViewModel.isBackupInProgress();

    if (backupNotInProgress && !mAppQueueViewModel.doesNotHaveBackups()) {

      if (!mAppQueueViewModel.getCurrentSelectionState().equals(
              SelectionState.SELECTION_STARTED)) {

        mAppQueueViewModel.setItemSelectionStateTo(
            SelectionState.SELECTION_STARTED);
      }

    } else {
      displayBackupErrorMessage(backupNotInProgress);
    }
  }

  private void removeSelectedBackups() {
    if (mAppQueueViewModel.hasSelectedItems()) {
      mAppQueueViewModel.removeSelectedApks();
    }
  }
}
