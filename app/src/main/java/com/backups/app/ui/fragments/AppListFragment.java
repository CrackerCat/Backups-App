package com.backups.app.ui.fragments;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.backups.app.R;
import com.backups.app.data.pojos.ApkFile;
import com.backups.app.data.viewmodels.apklist.ApkListViewModel;
import com.backups.app.data.viewmodels.appqueue.AppQueueViewModel;
import com.backups.app.ui.adapters.AppListAdapter;
import com.backups.app.ui.adapters.ItemClickListener;
import java.util.Collections;
import java.util.List;

public final class AppListFragment
    extends Fragment implements ItemClickListener {
  private ApkListViewModel mAppListViewModel;
  private AppQueueViewModel mAppQueueViewModel;

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

    initializeRecyclerView(activity);

    registerObservers();
  }

  @Override
  public void onItemClick(View view, int position) {
    if (!mAppQueueViewModel.isBackupInProgress()) {
      final ApkFile selected = mAppListAdapter.getItem(position);

      mAppQueueViewModel.addApp(selected);
    }
  }

  private void registerObservers() {
    mAppListViewModel.getApkListLiveData().observe(
        getViewLifecycleOwner(), this::onApkDataFetch);
  }

  private void onApkDataFetch(final List<ApkFile> apkFiles) {
    final boolean queryingForDataSet = apkFiles == null;

    if (queryingForDataSet) {
      if (mProgressBar.getVisibility() != View.VISIBLE) {
        showProgressBar();
      }

    } else if (!apkFiles.isEmpty()) {

      showCompletion();

      mAppListAdapter.changeDataSet(apkFiles);

    } else {
      showErrorMessage(
          getResources().getString(R.string.unable_to_fetch_apk_data_message));
    }
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
        new ViewModelProvider(activity).get(AppQueueViewModel.class);
  }

  private void initializeRecyclerView(final FragmentActivity activity) {
    mAppListAdapter = new AppListAdapter(Collections.emptyList());

    mAppListRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

    mAppListRecyclerView.setAdapter(mAppListAdapter);

    mAppListAdapter.setClickListener(this);
  }

  private void showErrorMessage(final String message) {
    mProgressBar.setVisibility(View.GONE);

    mAppListRecyclerView.setVisibility(View.GONE);

    mErrorMessageTV.setVisibility(View.VISIBLE);

    mErrorMessageTV.setText(message);
  }

  private void showProgressBar() {
    mProgressBar.setVisibility(View.VISIBLE);

    mAppListRecyclerView.setVisibility(View.GONE);
  }

  private void showCompletion() {
    mProgressBar.setVisibility(View.GONE);

    mAppListRecyclerView.setVisibility(View.VISIBLE);
  }
}
