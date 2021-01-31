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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backups.app.R;
import com.backups.app.data.APKFile;
import com.backups.app.data.APKFileRepository;
import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.data.SelectedAPK;
import com.backups.app.data.ViewModelFactory;
import com.backups.app.ui.adapters.AppListAdapter;

import java.util.List;
import java.util.concurrent.Executors;

public class AppListFragment extends Fragment implements AppListAdapter.ItemClickListener {

    private OnFragmentInteractionListener mListener;

    private ApkListViewModel mAppListViewModel;
    private AppQueueViewModel mAppQueueViewModel;

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

        initializeViews(view);

        initializeViewModels(activity);

        if (mAppListViewModel.hasNotReceivedApkList()) {
            showProgressBar();
        }

        mAppListViewModel.getApkData().observe(getViewLifecycleOwner(), apkFiles -> {
            if (!apkFiles.isEmpty()) {

                if (mAppListViewModel.hasNotReceivedApkList()) {
                    mAppListViewModel.receivedApkList(true);
                }
                initializeRecyclerView(activity, apkFiles);
                showCompletion();
            } else {
                showErrorMessage();
            }
        });
    }

    private void initializeViews(View view) {
        mAppRecyclerView = view.findViewById(R.id.recyclerview);
        mProgressBar = view.findViewById(R.id.progressbar);
        mTextView = view.findViewById(R.id.no_apps_tv);
    }

    private void initializeViewModels(FragmentActivity activity) {
        APKFileRepository apkFileRepository = new APKFileRepository(Executors.newSingleThreadExecutor());
        mAppListViewModel = new ViewModelProvider(activity, new ViewModelFactory(activity.getPackageManager(), apkFileRepository)).get(ApkListViewModel.class);
        mAppQueueViewModel = new ViewModelProvider(activity).get(AppQueueViewModel.class);
    }

    private void initializeRecyclerView(FragmentActivity activity, List<APKFile> data) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        mAppListAdapter = new AppListAdapter(data);
        mAppListAdapter.setClickListener(this);
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

    @Override
    public void onItemClick(View view, int position) {
        APKFile selected = mAppListAdapter.getItem(position);
        mAppQueueViewModel.push(new SelectedAPK(selected.getName(), selected.getAppSize()));
        mListener.onCall();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new ClassCastException(context.getString(R.string.fragment_listener_cast_error));
        }
    }

    @Override
    public void onDestroy() {
        mListener = null;
        super.onDestroy();
    }
}
