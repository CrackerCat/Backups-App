package com.backups.app.ui.fragments;

import static com.backups.app.Constants.REMOVE_FROM;

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
import com.backups.app.data.events.DataEvent;
import com.backups.app.data.events.SelectionState;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.viewmodels.appqueue.AppQueueViewModel;
import com.backups.app.ui.adapters.AppQueueAdapter;
import com.backups.app.ui.adapters.ItemClickListener;

public final class AppQueueFragment
    extends Fragment implements ItemClickListener {

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

    initializeViewModels(parent);

    initializeRecyclerView(view, parent);

    registerObservers();
  }

  @Override
  public void onItemClick(View view, int position) {
    mAppQueueViewModel.updateSelectedApks(mAppQueueAdapter.getItemAt(position));
  }

  private void initializeViewModels(final FragmentActivity parent) {
    mAppQueueViewModel =
        new ViewModelProvider(parent).get(AppQueueViewModel.class);
  }

  private void initializeRecyclerView(final View view,
                                      final FragmentActivity parent) {
    mAppQueueRecyclerView = view.findViewById(R.id.app_queue_rv);

    mAppQueueAdapter =
        new AppQueueAdapter(mAppQueueViewModel.getAppsInQueue(),
                            getResources().getColor(R.color.secondaryColor), getResources().getColor(R.color.primaryLightColor));

    mAppQueueRecyclerView.setLayoutManager(new LinearLayoutManager(parent));

    mAppQueueRecyclerView.setAdapter(mAppQueueAdapter);
  }

  private void registerObservers() {
    /* Any checks against DataEvent/ItemSelectionState.NONE are used only
   to prevent the callback from being handled during Activity recreation
   which is not needed
   **/

    mAppQueueViewModel.getSelectionStateLiveData().observe(
        getViewLifecycleOwner(), this::onSelectionStateChanged);

    mAppQueueViewModel.getDataEventLiveData().observe(
        getViewLifecycleOwner(), dataEvent -> {
          if (mAppQueueViewModel.getLastDataEvent() != DataEvent.NONE) {
            onAppQueueDataChanged(dataEvent);
          }
        });

    mAppQueueViewModel.getBackupProgressLiveData().observe(
        getViewLifecycleOwner(), progress -> {
          if (progress.getState() != BackupProgress.ProgressState.NONE) {
            onBackupProgressed(progress);

          } else if (mAppQueueViewModel.doesNotHaveBackups()) {
            mAppQueueViewModel.resetProgress();
          }
        });
  }

  private void onAppQueueDataChanged(final DataEvent dataEvent) {
    if (dataEvent == DataEvent.ITEM_ADDED_TO_QUEUE) {

      mAppQueueAdapter.notifyItemInserted(
          mAppQueueViewModel.getAppsInQueue().size());

    } else if (dataEvent == DataEvent.ABOUT_TO_MODIFY_ENTIRE_SELECTION) {

      mAppQueueRecyclerView.setClickable(false);

    } else if (dataEvent == DataEvent.ALL_ITEMS_SELECTED ||
               dataEvent == DataEvent.ALL_ITEMS_DESELECTED) {

      mAppQueueAdapter.notifyItemRangeChanged(
          0, mAppQueueViewModel.getAppsInQueue().size());
      mAppQueueRecyclerView.setClickable(true);

    } else if (dataEvent == DataEvent.ITEMS_REMOVED_FROM_QUEUE ||
               dataEvent == DataEvent.ITEMS_REMOVED_FROM_SELECTION) {
      mAppQueueAdapter.notifyDataSetChanged();

      mAppQueueRecyclerView.setClickable(true);
    }
  }

  private void onBackupProgressed(final BackupProgress progress) {
    if (progress.finished()) {
      mAppQueueAdapter.notifyItemRemoved(REMOVE_FROM);

    } else {
      mAppQueueAdapter.updateBackupViewHolder(mAppQueueRecyclerView,
                                              progress.getProgress());
    }
  }

  private void onSelectionStateChanged(final SelectionState selectionState) {
    if (selectionState == SelectionState.SELECTION_STARTED) {
      mAppQueueAdapter.setClickListener(this);
    } else if (selectionState == SelectionState.SELECTION_ENDED) {
      mAppQueueAdapter.setClickListener(null);
    }
  }
}