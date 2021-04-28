package com.backups.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.backups.app.data.BackupProgress;
import com.backups.app.data.viewmodels.ApkListViewModel;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.adapters.AppListAdapter;
import com.backups.app.ui.adapters.ItemClickListener;

import java.util.List;

import static com.backups.app.ui.Constants.APP_LIST;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;

public class AppListFragment extends Fragment implements ItemClickListener {
  private ApkListViewModel mAppListViewModel;
  private AppQueueViewModel mAppQueueViewModel;

  private ActionPresenter.IActionAvailability mActionNotifier;
  private AppListAdapter mAppListAdapter;
  private RecyclerView mAppListRecyclerView;

  private ProgressBar mProgressBar;
  private TextView mErrorMessageTV;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.app_list_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view,
                            @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    FragmentActivity activity = requireActivity();

    initializeViews(view);

    initializeViewModels(activity);

    mAppListViewModel.getApkListLiveData().observe(
        getViewLifecycleOwner(), apkFiles -> {
          if (!apkFiles.isEmpty()) {
            if (mProgressBar.getVisibility() == View.GONE) {
              showProgressBar();
            }

            if (mAppListAdapter != null) {
              mAppListAdapter.changeDataSet(apkFiles);
            } else {
              setupRecyclerView(activity, apkFiles);
            }

            mActionNotifier.makeActionAvailable(APP_LIST, SEARCH_BUTTON, true);

            showCompletion();

          } else {
            showErrorMessage(getResources().getString(
                R.string.unable_to_fetch_apk_data_message));
          }
        });

    mAppQueueViewModel.getBackupProgressLiveData().observe(
        getViewLifecycleOwner(), progress -> {
          if (getLifecycle().getCurrentState() != Lifecycle.State.CREATED) {
            handleBackupProgress(progress);
          }
        });
  }

  private void initializeViews(View view) {
    mProgressBar = view.findViewById(R.id.app_list_pb);
    mErrorMessageTV = view.findViewById(R.id.app_list_no_apps_tv);
    mAppListRecyclerView = view.findViewById(R.id.app_list_rv);
  }

  private void initializeViewModels(FragmentActivity activity) {
    mAppListViewModel =
        new ViewModelProvider(activity).get(ApkListViewModel.class);
    mAppQueueViewModel =
        new ViewModelProvider(activity, new BackupsViewModelFactory(activity))
            .get(AppQueueViewModel.class);
  }

  private void setupRecyclerView(FragmentActivity activity,
                                 List<APKFile> data) {
    LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

    mAppListAdapter = new AppListAdapter(data);

    mAppListAdapter.setClickListener(this);

    mAppListRecyclerView.setLayoutManager(layoutManager);

    mAppListRecyclerView.setAdapter(mAppListAdapter);
  }

  private void showErrorMessage(final String message) {
    mProgressBar.setVisibility(View.GONE);
    mAppListRecyclerView.setVisibility(View.GONE);
    mErrorMessageTV.setVisibility(View.VISIBLE);
    mErrorMessageTV.setText(message);
  }

  private void showProgressBar() {
    mProgressBar.setVisibility(View.VISIBLE);
    mAppListRecyclerView.setVisibility(View.INVISIBLE);
  }

  private void showCompletion() {
    mProgressBar.setVisibility(View.GONE);
    mAppListRecyclerView.setVisibility(View.VISIBLE);
  }

  private void handleBackupProgress(BackupProgress progress) {
    BackupProgress.ProgressState status = progress.state;

    boolean finished = status.equals(BackupProgress.ProgressState.FINISHED) ||
                       status.equals(BackupProgress.ProgressState.ERROR) &&
                           mAppQueueViewModel.doesNotHaveBackups();

    if (finished) {
      mActionNotifier.makeActionAvailable(APP_LIST, SEARCH_BUTTON, false);
    }
  }

  @Override
  public void onItemClick(View view, int position) {
    if (!mAppQueueViewModel.isBackupInProgress()) {
      APKFile selected = mAppListAdapter.getItem(position);

      mAppQueueViewModel.addApp(selected);
      mAppQueueViewModel.updateSelection(
          AppQueueViewModel.DataEvent.ITEM_ADDED);
    }
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    if (context instanceof ActionPresenter.IActionAvailability) {
      mActionNotifier = (ActionPresenter.IActionAvailability)context;
    } else {
      String listenerCastErrorMessage =
          "[AppListFragment]: Unable to cast to required class";
      throw new ClassCastException(listenerCastErrorMessage);
    }
  }

  @Override
  public void onDestroy() {
    mActionNotifier = null;
    super.onDestroy();
  }
}
