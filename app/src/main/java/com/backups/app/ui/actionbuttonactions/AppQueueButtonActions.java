package com.backups.app.ui.actionbuttonactions;

import static com.backups.app.ui.Constants.BACKUP_BUTTON;
import static com.backups.app.ui.Constants.ITEM_SELECTION_BUTTON;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;

import android.content.res.Resources;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import com.backups.app.R;
import com.backups.app.data.pojos.PreferenceKeys;
import com.backups.app.data.repositories.BackupRepository;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.ItemSelectionState;
import com.backups.app.ui.actions.IActionButton;
import com.backups.app.ui.actions.IActionButtonMethods;
import com.backups.app.ui.fragments.SearchDialogFragment;
import com.backups.app.utils.IDefaultAction;

public final class AppQueueButtonActions implements IActionButtonMethods {
  private final PreferenceKeys mPreferenceKeys;

  private final FragmentActivity mParentActivity;

  private final AppQueueViewModel mAppQueueViewModel;

  private final BackupsViewModel mBackupsViewModel;

  public AppQueueButtonActions(final FragmentActivity fragmentActivity,
                               final PreferenceKeys preferenceKeys) {
    mPreferenceKeys = preferenceKeys;

    mParentActivity = fragmentActivity;

    mAppQueueViewModel =
        new ViewModelProvider(fragmentActivity).get(AppQueueViewModel.class);

    mBackupsViewModel =
        new ViewModelProvider(fragmentActivity,
                              new BackupsViewModelFactory(fragmentActivity))
            .get(BackupsViewModel.class);
  }

  private void
  shouldDefaultToPrimaryStorage(final BackupRepository.OutputStorage status) {
    if (status.equals(BackupRepository.OutputStorage.USING_DEFAULT_STORAGE)) {
      final int storageVolumeIndex =
          (mPreferenceKeys.outputDirectory = BackupRepository.sPrimaryStorage);
      Resources resources = mParentActivity.getResources();

      Toast
          .makeText(mParentActivity,
                    resources.getString(R.string.storage_volume_not_present),
                    Toast.LENGTH_SHORT)
          .show();

      mBackupsViewModel.setStorageVolumeIndex(storageVolumeIndex);

      PreferenceManager.getDefaultSharedPreferences(mParentActivity)
          .edit()
          .putString(resources.getString(R.string.output_directory_key),
                     String.valueOf(storageVolumeIndex))
          .apply();
    }
  }

  private Pair<Boolean, String> startPreBackupChecks() {
    boolean isBackupInProgress = mBackupsViewModel.isBackupInProgress();

    boolean doesNotHaveSufficientStorage =
        !mBackupsViewModel.hasSufficientStorage();

    String outputMessage;
    boolean canBackup = false;

    Resources resources = mParentActivity.getResources();

    if (isBackupInProgress) {
      outputMessage = resources.getString(R.string.backup_in_progress_message);
    } else if (doesNotHaveSufficientStorage) {
      outputMessage =
          resources.getString(R.string.insufficient_storage_message);
    } else {
      String startingBackupMessage =
          resources.getString(R.string.commencing_backup_message);

      outputMessage = String.format(startingBackupMessage,
                                    mAppQueueViewModel.getAppsInQueue().size());

      canBackup = true;
    }

    return new Pair<>(canBackup, outputMessage);
  }

  private void interruptSelectionState() {
    if (mAppQueueViewModel.getCurrentSelectionState().equals(
            ItemSelectionState.SELECTION_STARTED)) {

      if (mAppQueueViewModel.hasSelectedItems()) {
        mAppQueueViewModel.clearSelection();

        mAppQueueViewModel.setItemSelectionStateTo(
            ItemSelectionState.SELECTION_ENDED);
      }
    }
  }

  private void attemptBackup() {
    BackupRepository.OutputStorage status =
        mBackupsViewModel.isOutputDirectoryMounted();

    if (status.equals(BackupRepository.OutputStorage.MOUNTED) ||
        status.equals(BackupRepository.OutputStorage.USING_DEFAULT_STORAGE)) {

      shouldDefaultToPrimaryStorage(status);

      final Pair<Boolean, String> canBackup = startPreBackupChecks();

      final String outputMessage = canBackup.second;

      if (canBackup.first && outputMessage != null) {
        Toast.makeText(mParentActivity, outputMessage, Toast.LENGTH_SHORT)
            .show();

        interruptSelectionState();

        mBackupsViewModel.startBackup(mAppQueueViewModel.getAppsInQueue());
      }
    } else if (status.equals(
                   BackupRepository.OutputStorage.NO_MOUNTED_DEVICES)) {
      Toast
          .makeText(mParentActivity,
                    mParentActivity.getResources().getString(
                        R.string.no_storage_volumes_present),
                    Toast.LENGTH_SHORT)
          .show();
    } else if (status.equals(
                   BackupRepository.OutputStorage.UNKNOWN_STORAGE_VOLUME)) {
      Toast
          .makeText(mParentActivity,
                    mParentActivity.getResources().getString(
                        R.string.unknown_storage_volume),
                    Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void setupForSelection() {
    boolean backupNotInProgress = !mBackupsViewModel.isBackupInProgress();

    boolean canStartSelection =
        !mAppQueueViewModel.doesNotHaveBackups() && backupNotInProgress;

    if (canStartSelection) {
      if (!mAppQueueViewModel.getCurrentSelectionState().equals(
              ItemSelectionState.SELECTION_STARTED)) {

        mAppQueueViewModel.setItemSelectionStateTo(
            ItemSelectionState.SELECTION_STARTED);
      }
    } else {
      Resources resources = mParentActivity.getResources();

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
  }

  @Override
  public void initializeActionButtonActions(int position, IActionButton action,
                                            IDefaultAction defaultChecks) {
    View.OnClickListener inactiveActions =
        (defaultChecks != null ? v -> defaultChecks.invoke() : v -> {});

    if (position == SEARCH_BUTTON) {

      action.assignCallBacks(v -> {
        SearchDialogFragment appSearchDialog = new SearchDialogFragment();

        appSearchDialog.setDataSetID(SearchDialogFragment.DataSet.APP_QUEUE);

        appSearchDialog.show(mParentActivity.getSupportFragmentManager(),
                             (appSearchDialog.getClass().getSimpleName()));
      }, inactiveActions);

    } else if (position == BACKUP_BUTTON) {
      action.assignCallBacks(v -> attemptBackup(), inactiveActions);

    } else if (position == ITEM_SELECTION_BUTTON) {
      action.assignCallBacks(v -> {
        if (mAppQueueViewModel.hasSelectedItems()) {
          mAppQueueViewModel.clearAndEmptySelection();
        }
      }, v -> setupForSelection());
    }
  }
}
