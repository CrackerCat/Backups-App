package com.backups.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.backups.app.ui.adapters.AppQueueAdapter;
import java.util.List;

public class AppQueueFragment extends Fragment {

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
        new ViewModelProvider(parent).get(AppQueueViewModel.class);

    mAppQueueRecyclerView = view.findViewById(R.id.app_queue_rv);

    initializeRecyclerView(parent);

    mAppQueueViewModel.getAppQueueLiveData().observe(
        getViewLifecycleOwner(), appQueue -> {
          if (getLifecycle().getCurrentState() != Lifecycle.State.CREATED) {
            mAppQueueAdapter.notifyDataSetChanged();
          }
        });
  }

  private void initializeRecyclerView(FragmentActivity parent) {
    LinearLayoutManager layout = new LinearLayoutManager(parent);
    List<APKFile> queue = mAppQueueViewModel.getSelectedApps();
    mAppQueueAdapter = new AppQueueAdapter(queue);
    mAppQueueRecyclerView.setLayoutManager(layout);
    mAppQueueRecyclerView.setAdapter(mAppQueueAdapter);
  }
}
