package com.backups.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backups.app.R;
import com.backups.app.data.APKFile;
import com.backups.app.data.BackupProgress;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.adapters.AppQueueAdapter;
import com.backups.app.utils.PackageNameUtils;

import java.util.List;

import static com.backups.app.ui.Constants.APP_LIST;
import static com.backups.app.ui.Constants.APP_QUEUE;
import static com.backups.app.ui.Constants.BACKUP_BUTTON;
import static com.backups.app.ui.Constants.REMOVE_FROM;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;

public class AppQueueFragment extends Fragment {

  private ActionPresenter.IActionAvailability mActionNotifier;
  private AppQueueViewModel mAppQueueViewModel;

  private RecyclerView mAppQueueRecyclerView;
  private AppQueueAdapter mAppQueueAdapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.app_queue_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view,
                            @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    FragmentActivity parent = requireActivity();

    mAppQueueViewModel =
        new ViewModelProvider(parent, new BackupsViewModelFactory(parent))
            .get(AppQueueViewModel.class);

    initializeRecyclerView(view, parent);

    mAppQueueViewModel.getAppQueueLiveData().observe(
        getViewLifecycleOwner(), appQueue -> {
          boolean wasItemAdded = mAppQueueViewModel.getLastDataEvent().equals(
              AppQueueViewModel.DataEvent.ITEM_ADDED);
          if (wasItemAdded) {
            mAppQueueAdapter.notifyItemInserted(appQueue.size());
          }
        });

    mAppQueueViewModel.getBackupProgressLiveData().observe(
        getViewLifecycleOwner(), backupProgress -> {
          boolean backupStarted =
              !backupProgress.state.equals(BackupProgress.ProgressState.NONE);
          if (backupStarted) {
            handleBackupProgress(backupProgress);
          }
        });
  }

  private void initializeRecyclerView(View view, FragmentActivity parent) {
    mAppQueueRecyclerView = view.findViewById(R.id.app_queue_rv);

    LinearLayoutManager layout = new LinearLayoutManager(parent);

    List<APKFile> queue = mAppQueueViewModel.getSelectedApps();

    mAppQueueAdapter = new AppQueueAdapter(queue);

    mAppQueueRecyclerView.setLayoutManager(layout);

    mAppQueueRecyclerView.setAdapter(mAppQueueAdapter);

    new ItemTouchHelper(mItemTouchHelperCallback)
        .attachToRecyclerView(mAppQueueRecyclerView);
  }

  private void makeActionsAvailable(final boolean decision) {
    mActionNotifier.makeActionAvailable(APP_QUEUE, SEARCH_BUTTON, decision);
    mActionNotifier.makeActionAvailable(APP_QUEUE, BACKUP_BUTTON, decision);
  }

  private void makeAppListActionsAvailable(final boolean decision) {
    mActionNotifier.makeActionAvailable(APP_LIST, SEARCH_BUTTON, decision);
  }

  private void handleBackupProgress(BackupProgress progress) {
    BackupProgress.ProgressState progressState = progress.state;

    boolean updateRecyclerView =
        progressState == BackupProgress.ProgressState.FINISHED ||
        progressState == BackupProgress.ProgressState.ERROR;

    if (updateRecyclerView) {
      mAppQueueAdapter.notifyItemRemoved(REMOVE_FROM);

      if (mAppQueueViewModel.doesNotHaveBackups()) {
        makeActionsAvailable(false);
        makeAppListActionsAvailable(true);
      }

    } else {
      AppQueueAdapter.BackupsViewHolder backupsViewHolder =
          (AppQueueAdapter.BackupsViewHolder)mAppQueueRecyclerView
              .findViewHolderForAdapterPosition(REMOVE_FROM);

      backupsViewHolder.updateProgressBy(progress.progress);
    }
  }

  ItemTouchHelper.SimpleCallback mItemTouchHelperCallback =
      new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |
                                                ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
          return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                             int direction) {
          int position = viewHolder.getAdapterPosition();

          List<APKFile> selectedApps = mAppQueueViewModel.getSelectedApps();

          PackageNameUtils.resetCountFor(
              selectedApps.remove(position).getName());

          mAppQueueAdapter.notifyItemRemoved(position);

          if (selectedApps.isEmpty()) {
            makeActionsAvailable(false);
          }

          mAppQueueViewModel.updateSelection(
              AppQueueViewModel.DataEvent.ITEM_REMOVED);
        }
      };

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    if (context instanceof ActionPresenter.IActionAvailability) {
      mActionNotifier = (ActionPresenter.IActionAvailability)context;
    } else {
      String listenerCastErrorMessage =
          "[AppQueueFragment]: Unable to cast to required class";
      throw new ClassCastException(listenerCastErrorMessage);
    }
  }

  @Override
  public void onDestroy() {
    mActionNotifier = null;
    super.onDestroy();
  }
}
