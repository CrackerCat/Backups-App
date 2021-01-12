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
import com.backups.app.data.APKFile;
import com.backups.app.data.APKFileRepository;
import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.ViewModelFactory;
import com.backups.app.ui.adapters.AppListAdapter;

import java.util.List;
import java.util.concurrent.Executors;

public class AppListFragment extends Fragment {

    private ApkListViewModel mAppListViewModel;

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mAppRecyclerView;
    private AppListAdapter mAppListAdapter;

    private ProgressBar mProgressBar;
    private TextView mTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentActivity activity = requireActivity();
        mAppRecyclerView = view.findViewById(R.id.recyclerview);
        mProgressBar = view.findViewById(R.id.progressbar);
        mTextView = view.findViewById(R.id.no_apps_tv);

        APKFileRepository mApkFileRepository = new APKFileRepository(Executors.newSingleThreadExecutor());
        mAppListViewModel = new ViewModelProvider(activity, new ViewModelFactory(activity.getPackageManager(), mApkFileRepository)).get(ApkListViewModel.class);

        if (!mAppListViewModel.hasRecievedApkList()) {
            showProgressBar();
        }

        mAppListViewModel.getApkData().observe(getViewLifecycleOwner(), apkFiles -> {
            if (!apkFiles.isEmpty()) {

                if (!mAppListViewModel.hasRecievedApkList()) {
                    mAppListViewModel.recievedApkList(true);
                }
                initRecyclerView(activity, apkFiles);
                showCompletion();
            } else {
                showErrorMessage();
            }
        });
    }

    private void initRecyclerView(FragmentActivity activity, List<APKFile> data) {
        mLayoutManager = new LinearLayoutManager(activity);
        mAppListAdapter = new AppListAdapter(data);
        mAppRecyclerView.setLayoutManager(mLayoutManager);
        mAppRecyclerView.setAdapter(mAppListAdapter);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mAppRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        mProgressBar.setVisibility(ViewGroup.INVISIBLE);
        mTextView.setVisibility(View.VISIBLE);
    }

    private void showCompletion() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mAppRecyclerView.setVisibility(View.VISIBLE);
    }
}
