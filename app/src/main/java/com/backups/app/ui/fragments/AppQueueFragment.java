package com.backups.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backups.app.R;
import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.DataEvent;
import com.backups.app.data.viewmodels.ItemSelectionState;
import com.backups.app.ui.adapters.AppQueueAdapter;
import com.backups.app.ui.adapters.ItemClickListener;

import java.util.List;

import static com.backups.app.ui.Constants.REMOVE_FROM;

public class AppQueueFragment extends Fragment implements ItemClickListener {

  private AppQueueViewModel mAppQueueViewModel;
  private BackupsViewModel mBackupsViewModel;

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

    initializeViewModels(parent);

    initializeRecyclerView(view, parent);

    registerObservers();
  }

  @Override
  public void onItemClick(View view, int position) {
    mAppQueueViewModel.addOrRemoveSelection(
        mAppQueueAdapter.getItemAt(position));
  }

  private void initializeViewModels(final FragmentActivity parent) {
    mAppQueueViewModel =
        new ViewModelProvider(parent).get(AppQueueViewModel.class);

    mBackupsViewModel =
        new ViewModelProvider(parent, new BackupsViewModelFactory(parent))
            .get(BackupsViewModel.class);
  }

  private void initializeRecyclerView(View view, FragmentActivity parent) {
    mAppQueueRecyclerView = view.findViewById(R.id.app_queue_rv);

    LinearLayoutManager layout = new LinearLayoutManager(parent);

    List<APKFile> queue = mAppQueueViewModel.getAppsInQueue();

    mAppQueueAdapter = new AppQueueAdapter(
        queue, getResources().getColor(R.color.secondaryColor));

    mAppQueueRecyclerView.setLayoutManager(layout);

    mAppQueueRecyclerView.setAdapter(mAppQueueAdapter);
  }

  private void registerObservers() {
    /* Any checks against DataEvent/ItemSelectionState.NONE are used only
   to prevent the callback from being handled during Activity recreation
   which is not needed
   **/

    mAppQueueViewModel.getSelectionStateLiveData().observe(
        getViewLifecycleOwner(), state -> {
          if (state.equals(ItemSelectionState.SELECTION_STARTED)) {
            mAppQueueAdapter.setClickListener(this);
          } else if (state.equals(ItemSelectionState.SELECTION_ENDED)) {
            mAppQueueAdapter.setClickListener(null);
          }
        });

    mAppQueueViewModel.getDataEventLiveData().observe(
        getViewLifecycleOwner(), dataEvent -> {
          if (!mAppQueueViewModel.getLastDataEvent().equals(DataEvent.NONE)) {
            if (dataEvent.equals(DataEvent.ITEM_ADDED_TO_QUEUE)) {

              mAppQueueAdapter.notifyItemInserted(
                  mAppQueueViewModel.getAppsInQueue().size() - 1);

            } else if (dataEvent.equals(
                           DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION)) {

              mAppQueueRecyclerView.setClickable(false);

            } else if (dataEvent.equals(DataEvent.ALL_ITEMS_SELECTED) ||
                       dataEvent.equals(DataEvent.ALL_ITEMS_DESELECTED) ||
                       dataEvent.equals(DataEvent.ITEMS_REMOVED_FROM_QUEUE) ||
                       dataEvent.equals(
                           DataEvent.ITEMS_REMOVED_FROM_SELECTION)) {

              mAppQueueAdapter.notifyDataSetChanged();

              mAppQueueRecyclerView.setClickable(true);
            }
          }
        });

    mBackupsViewModel.getBackupProgressLiveData().observe(
        getViewLifecycleOwner(), progress -> {
          boolean backupStarted =
              !progress.getState().equals(BackupProgress.ProgressState.NONE);

          if (backupStarted) {
            handleBackupProgress(progress);
          }
        });
  }

  private void handleBackupProgress(BackupProgress progress) {
    BackupProgress.ProgressState progressState = progress.getState();

    if (progressState == BackupProgress.ProgressState.ONGOING) {
      AppQueueAdapter.BackupsViewHolder backupsViewHolder =
          (AppQueueAdapter.BackupsViewHolder)mAppQueueRecyclerView
              .findViewHolderForAdapterPosition(REMOVE_FROM);

      if (backupsViewHolder != null) {
        backupsViewHolder.updateProgressBy(progress.getProgress());
      }
    } else if (progressState == BackupProgress.ProgressState.FINISHED ||
               progressState == BackupProgress.ProgressState.ERROR) {
      mAppQueueAdapter.notifyItemRemoved(REMOVE_FROM);

      if (mAppQueueViewModel.doesNotHaveBackups()) {
        mBackupsViewModel.endBackup();
      }

    } else if (progressState == BackupProgress.ProgressState.ENDED) {
      mBackupsViewModel.resetProgress();
    }
  }
}
