package com.backups.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backups.app.R;
import com.backups.app.data.APKFile;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.data.BackupProgress;
import com.backups.app.data.BackupsViewModelFactory;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.adapters.AppQueueAdapter;

import java.util.List;

import static com.backups.app.ui.Constants.APP_QUEUE;
import static com.backups.app.ui.Constants.BACKUP_BUTTON;
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

    mAppQueueRecyclerView = view.findViewById(R.id.app_queue_rv);

    initializeRecyclerView(parent);

    mAppQueueViewModel.getAppQueueLiveData().observe(
        getViewLifecycleOwner(), appQueue -> {
          if (getLifecycle().getCurrentState() != Lifecycle.State.CREATED) {
            mAppQueueAdapter.addedItem();
          }
        });

    mAppQueueViewModel.getBackupProgressLiveData().observe(
        getViewLifecycleOwner(), this::handleBackupProgress);
  }

  private void initializeRecyclerView(FragmentActivity parent) {
    LinearLayoutManager layout = new LinearLayoutManager(parent);

    List<APKFile> queue = mAppQueueViewModel.getSelectedApps();

    mAppQueueAdapter = new AppQueueAdapter(queue);

    mAppQueueRecyclerView.setLayoutManager(layout);

    mAppQueueRecyclerView.setAdapter(mAppQueueAdapter);
  }

  private void handleBackupProgress(BackupProgress progress) {
    int position = 0;
    BackupProgress.ProgressState progressState = progress.state;

    boolean updateRecyclerView =
        progressState == BackupProgress.ProgressState.FINISHED ||
        progressState == BackupProgress.ProgressState.ERROR;

    if (updateRecyclerView) {
      mAppQueueAdapter.removedItem();

      if (!mAppQueueViewModel.hasBackups()) {
        mActionNotifier.makeActionAvailable(APP_QUEUE, SEARCH_BUTTON, false);
        mActionNotifier.makeActionAvailable(APP_QUEUE, BACKUP_BUTTON, false);
      }

    } else {
      View toUpdate = mAppQueueRecyclerView.getChildAt(position);

      ProgressBar progressBar = toUpdate.findViewById(R.id.app_queue_item_pb);

      progressBar.incrementProgressBy(progress.progress);
    }
  }

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
