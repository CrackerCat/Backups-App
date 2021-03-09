package com.backups.app.ui.fragments;

import static com.backups.app.ui.Constants.SEARCH_BUTTON;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.backups.app.R;
import com.backups.app.data.APKFile;
import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.adapters.AppListAdapter;
import com.backups.app.ui.adapters.ItemClickListener;
import java.util.List;

public class AppListFragment extends Fragment implements ItemClickListener {
  private ApkListViewModel mAppListViewModel;
  private AppQueueViewModel mAppQueueViewModel;

  private ActionPresenter.IActionAvailability mActionNotifier;
  private AppListAdapter mAppListAdapter;
  private RecyclerView mAppListRecyclerView;

  private ProgressBar mProgressBar;
  private TextView mTextView;

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
          if (apkFiles != null) {
            setupRecyclerView(activity, apkFiles);

            showCompletion();

            int available = mActionNotifier.totalAvailableActions();
            if (available != 0) {
              mActionNotifier.makeActionAvailable(SEARCH_BUTTON, true);
            }
          } else {
            showErrorMessage();
          }
        });
  }

  private void initializeViews(View view) {
    mProgressBar = view.findViewById(R.id.app_list_pb);
    mTextView = view.findViewById(R.id.app_list_no_apps_tv);
    mAppListRecyclerView = view.findViewById(R.id.app_list_rv);
  }

  private void initializeViewModels(FragmentActivity activity) {
    mAppListViewModel =
        new ViewModelProvider(activity).get(ApkListViewModel.class);
    mAppQueueViewModel =
        new ViewModelProvider(activity).get(AppQueueViewModel.class);
  }

  private void setupRecyclerView(FragmentActivity activity,
                                 List<APKFile> data) {
    LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
    mAppListAdapter = new AppListAdapter(data);
    mAppListAdapter.setClickListener(this);
    mAppListRecyclerView.setLayoutManager(layoutManager);
    mAppListRecyclerView.setAdapter(mAppListAdapter);
  }

  private void showErrorMessage() {
    mProgressBar.setVisibility(View.GONE);
    mTextView.setVisibility(View.VISIBLE);
  }

  private void showCompletion() {
    mProgressBar.setVisibility(View.GONE);
    mAppListRecyclerView.setVisibility(View.VISIBLE);
  }

  @Override
  public void onItemClick(View view, int position) {
    APKFile selected = mAppListAdapter.getItem(position);
    mAppQueueViewModel.addApp(selected);
    mAppQueueViewModel.updateSelection();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    if (context instanceof ActionPresenter.IActionAvailability) {
      mActionNotifier = (ActionPresenter.IActionAvailability)context;
    } else {
      throw new ClassCastException(
          context.getString(R.string.listener_cast_error_message));
    }
  }

  @Override
  public void onDestroy() {
    mActionNotifier = null;
    super.onDestroy();
  }
}
