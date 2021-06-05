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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backups.app.R;
import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.DataEvent;
import com.backups.app.data.viewmodels.ItemSelectionState;
import com.backups.app.ui.actions.ActionHost;
import com.backups.app.ui.adapters.AppQueueAdapter;
import com.backups.app.ui.adapters.ItemClickListener;

import java.util.List;

import static com.backups.app.ui.Constants.APP_LIST;
import static com.backups.app.ui.Constants.APP_QUEUE;
import static com.backups.app.ui.Constants.BACKUP_BUTTON;
import static com.backups.app.ui.Constants.ITEM_SELECTION_BUTTON;
import static com.backups.app.ui.Constants.REMOVE_FROM;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;

public class AppQueueFragment extends Fragment implements ItemClickListener {

  private ActionHost mActionHost;
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

    registerObservers();
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
                  mAppQueueViewModel.getAppsInQueue().size());

            } else if (dataEvent.equals(
                           DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION)) {

              mAppQueueRecyclerView.setClickable(false);

            } else if (dataEvent.equals(DataEvent.ALL_ITEMS_SELECTED) ||
                       dataEvent.equals(DataEvent.ALL_ITEMS_DESELECTED)) {

              mAppQueueAdapter.notifyDataSetChanged();

              mAppQueueRecyclerView.setClickable(true);

            } else if (dataEvent.equals(DataEvent.ITEMS_REMOVED_FROM_QUEUE) ||
                       dataEvent.equals(
                           DataEvent.ITEMS_REMOVED_FROM_SELECTION)) {

              if (mAppQueueViewModel.getAppsInQueue().isEmpty()) {
                makeActionsUnavailable();
              }

              mAppQueueAdapter.notifyDataSetChanged();

              mAppQueueRecyclerView.setClickable(true);
            }
          }
        });

    mAppQueueViewModel.getBackupProgressLiveData().observe(
        getViewLifecycleOwner(), progress -> {
          boolean backupStarted =
              !progress.getState().equals(BackupProgress.ProgressState.NONE);
          if (backupStarted) {
            handleBackupProgress(progress);
          }
        });
  }

  private void makeActionsUnavailable() {
    mActionHost.makeActionAvailable(APP_QUEUE, SEARCH_BUTTON, false);
    mActionHost.makeActionAvailable(APP_QUEUE, BACKUP_BUTTON, false);
    mActionHost.makeActionAvailable(APP_QUEUE, ITEM_SELECTION_BUTTON, false);
  }

  private void makeAppListActionsAvailable() {
    mActionHost.makeActionAvailable(APP_LIST, SEARCH_BUTTON, true);
  }

  private void handleBackupProgress(BackupProgress progress) {
    BackupProgress.ProgressState progressState = progress.getState();

    boolean updateRecyclerView =
        progressState == BackupProgress.ProgressState.FINISHED ||
        progressState == BackupProgress.ProgressState.ERROR;

    if (updateRecyclerView) {
      mAppQueueAdapter.notifyItemRemoved(REMOVE_FROM);

      if (mAppQueueViewModel.doesNotHaveBackups()) {
        makeActionsUnavailable();
        makeAppListActionsAvailable();
      }

    } else {
      AppQueueAdapter.BackupsViewHolder backupsViewHolder =
          (AppQueueAdapter.BackupsViewHolder)mAppQueueRecyclerView
              .findViewHolderForAdapterPosition(REMOVE_FROM);

      if (backupsViewHolder != null) {
        backupsViewHolder.updateProgressBy(progress.getProgress());
      }
    }
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    if (context instanceof ActionHost) {
      mActionHost = (ActionHost)context;
    } else {
      String listenerCastErrorMessage =
          "[AppQueueFragment]: Unable to cast to required class";
      throw new ClassCastException(listenerCastErrorMessage);
    }
  }

  @Override
  public void onDestroy() {
    mActionHost = null;
    super.onDestroy();
  }

  @Override
  public void onItemClick(View view, int position) {
    mAppQueueViewModel.addOrRemoveSelection(
        mAppQueueAdapter.getItemAt(position));
  }
}
